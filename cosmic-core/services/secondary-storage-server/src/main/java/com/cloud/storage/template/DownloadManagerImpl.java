package com.cloud.storage.template;

import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.exception.InternalErrorException;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.StorageLayer;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.command.DownloadCommand;
import com.cloud.storage.command.DownloadCommand.ResourceType;
import com.cloud.storage.command.DownloadProgressCommand;
import com.cloud.storage.command.DownloadProgressCommand.RequestType;
import com.cloud.storage.resource.SecondaryStorageResource;
import com.cloud.storage.template.Processor.FormatInfo;
import com.cloud.storage.template.TemplateDownloader.DownloadCompleteCallback;
import com.cloud.storage.template.TemplateDownloader.Status;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.StringUtils;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.Proxy;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;
import com.cloud.utils.storage.QCOW2Utils;

import javax.naming.ConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadManagerImpl extends ManagerBase implements DownloadManager {
    public static final Logger s_logger = LoggerFactory.getLogger(DownloadManagerImpl.class);
    private final Map<String, DownloadJob> jobs = new ConcurrentHashMap<>();
    public Map<String, Processor> _processors;
    StorageLayer _storage;
    private String _name;
    private String _templateDir;
    private String _volumeDir;
    private String createTmpltScr;
    private String createVolScr;
    private ExecutorService threadPool;
    private String listTmpltScr;
    private String listVolScr;
    private int installTimeoutPerGig = 180 * 60 * 1000;

    public DownloadManagerImpl() {
    }

    public static VMTemplateHostVO.Status convertStatus(final Status tds) {
        switch (tds) {
            case ABORTED:
                return VMTemplateHostVO.Status.NOT_DOWNLOADED;
            case DOWNLOAD_FINISHED:
                return VMTemplateHostVO.Status.DOWNLOAD_IN_PROGRESS;
            case IN_PROGRESS:
                return VMTemplateHostVO.Status.DOWNLOAD_IN_PROGRESS;
            case NOT_STARTED:
                return VMTemplateHostVO.Status.NOT_DOWNLOADED;
            case RECOVERABLE_ERROR:
                return VMTemplateHostVO.Status.NOT_DOWNLOADED;
            case UNKNOWN:
                return VMTemplateHostVO.Status.UNKNOWN;
            case UNRECOVERABLE_ERROR:
                return VMTemplateHostVO.Status.DOWNLOAD_ERROR;
            case POST_DOWNLOAD_FINISHED:
                return VMTemplateHostVO.Status.DOWNLOADED;
            default:
                return VMTemplateHostVO.Status.UNKNOWN;
        }
    }

    public void setThreadPool(final ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public void setStorageLayer(final StorageLayer storage) {
        _storage = storage;
    }

    /**
     * Get notified of change of job status. Executed in context of downloader
     * thread
     *
     * @param jobId  the id of the job
     * @param status the status of the job
     */
    public void setDownloadStatus(final String jobId, final Status status) {
        final DownloadJob dj = jobs.get(jobId);
        if (dj == null) {
            s_logger.warn("setDownloadStatus for jobId: " + jobId + ", status=" + status + " no job found");
            return;
        }
        final TemplateDownloader td = dj.getTemplateDownloader();
        s_logger.info("Download Completion for jobId: " + jobId + ", status=" + status);
        s_logger.info("local: " + td.getDownloadLocalPath() + ", bytes=" + td.getDownloadedBytes() + ", error=" + td.getDownloadError() + ", pct=" +
                td.getDownloadPercent());

        switch (status) {
            case ABORTED:
            case NOT_STARTED:
            case UNRECOVERABLE_ERROR:
                // TODO
                dj.cleanup();
                break;
            case UNKNOWN:
                return;
            case IN_PROGRESS:
                s_logger.info("Resuming jobId: " + jobId + ", status=" + status);
                td.setResume(true);
                threadPool.execute(td);
                break;
            case RECOVERABLE_ERROR:
                threadPool.execute(td);
                break;
            case DOWNLOAD_FINISHED:
                // For other TemplateDownloaders where files are locally available,
                // we run the postLocalDownload() method.
                td.setDownloadError("Download success, starting install ");
                final String result = postLocalDownload(jobId);
                if (result != null) {
                    s_logger.error("Failed post download script: " + result);
                    td.setStatus(Status.UNRECOVERABLE_ERROR);
                    td.setDownloadError("Failed post download script: " + result);
                } else {
                    td.setStatus(Status.POST_DOWNLOAD_FINISHED);
                    td.setDownloadError("Install completed successfully at " + new SimpleDateFormat().format(new Date()));
                }

                dj.cleanup();
                break;
            default:
                break;
        }
    }

    /**
     * Post local download activity (install and cleanup). Executed in context of
     * downloader thread
     *
     * @throws IOException
     */
    private String postLocalDownload(final String jobId) {
        final DownloadJob dnld = jobs.get(jobId);
        final TemplateDownloader td = dnld.getTemplateDownloader();
        final String resourcePath = dnld.getInstallPathPrefix(); // path with mount
        // directory
        final String finalResourcePath = dnld.getTmpltPath(); // template download
        // path on secondary
        // storage
        final ResourceType resourceType = dnld.getResourceType();

        final File originalTemplate = new File(td.getDownloadLocalPath());
        final String checkSum = computeCheckSum(originalTemplate);
        if (checkSum == null) {
            s_logger.warn("Something wrong happened when try to calculate the checksum of downloaded template!");
        }
        dnld.setCheckSum(checkSum);

        int imgSizeGigs = (int) Math.ceil(_storage.getSize(td.getDownloadLocalPath()) * 1.0d / (1024 * 1024 * 1024));
        imgSizeGigs++; // add one just in case
        final long timeout = (long) imgSizeGigs * installTimeoutPerGig;
        Script scr = null;
        final String script = resourceType == ResourceType.TEMPLATE ? createTmpltScr : createVolScr;
        scr = new Script(script, timeout, s_logger);
        scr.add("-s", Integer.toString(imgSizeGigs));
        scr.add("-S", Long.toString(td.getMaxTemplateSizeInBytes()));
        if (dnld.getDescription() != null && dnld.getDescription().length() > 1) {
            scr.add("-d", dnld.getDescription());
        }
        if (dnld.isHvm()) {
            scr.add("-h");
        }

        // add options common to ISO and template
        final String extension = dnld.getFormat().getFileExtension();
        String templateName = "";
        if (extension.equals("iso")) {
            templateName = jobs.get(jobId).getTmpltName().trim().replace(" ", "_");
        } else {
            templateName = java.util.UUID.nameUUIDFromBytes((jobs.get(jobId).getTmpltName() + System.currentTimeMillis()).getBytes(StringUtils.getPreferredCharset())).toString();
        }

        // run script to mv the temporary template file to the final template
        // file
        final String templateFilename = templateName + "." + extension;
        dnld.setTmpltPath(finalResourcePath + "/" + templateFilename);
        scr.add("-n", templateFilename);

        scr.add("-t", resourcePath);
        scr.add("-f", td.getDownloadLocalPath()); // this is the temporary
        // template file downloaded
        if (dnld.getChecksum() != null && dnld.getChecksum().length() > 1) {
            scr.add("-c", dnld.getChecksum());
        }
        scr.add("-u"); // cleanup
        final String result;
        result = scr.execute();

        if (result != null) {
            return result;
        }

        // Set permissions for the downloaded template
        final File downloadedTemplate = new File(resourcePath + "/" + templateFilename);
        _storage.setWorldReadableAndWriteable(downloadedTemplate);

        // Set permissions for template/volume.properties
        String propertiesFile = resourcePath;
        if (resourceType == ResourceType.TEMPLATE) {
            propertiesFile += "/template.properties";
        } else {
            propertiesFile += "/volume.properties";
        }
        final File templateProperties = new File(propertiesFile);
        _storage.setWorldReadableAndWriteable(templateProperties);

        final TemplateLocation loc = new TemplateLocation(_storage, resourcePath);
        try {
            loc.create(dnld.getId(), true, dnld.getTmpltName());
        } catch (final IOException e) {
            s_logger.warn("Something is wrong with template location " + resourcePath, e);
            loc.purge();
            return "Unable to download due to " + e.getMessage();
        }

        final Iterator<Processor> en = _processors.values().iterator();
        while (en.hasNext()) {
            final Processor processor = en.next();

            FormatInfo info = null;
            try {
                info = processor.process(resourcePath, null, templateName);
            } catch (final InternalErrorException e) {
                s_logger.error("Template process exception ", e);
                return e.toString();
            }
            if (info != null) {
                if (!loc.addFormat(info)) {
                    loc.purge();
                    return "Unable to install due to invalid file format";
                }
                dnld.setTemplatesize(info.virtualSize);
                dnld.setTemplatePhysicalSize(info.size);
                break;
            }
        }

        if (!loc.save()) {
            s_logger.warn("Cleaning up because we're unable to save the formats");
            loc.purge();
        }

        return null;
    }

    private String computeCheckSum(final File f) {
        final byte[] buffer = new byte[8192];
        int read = 0;
        final MessageDigest digest;
        String checksum = null;
        InputStream is = null;
        try {
            digest = MessageDigest.getInstance("MD5");
            is = new FileInputStream(f);
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            final byte[] md5sum = digest.digest();
            final BigInteger bigInt = new BigInteger(1, md5sum);
            checksum = String.format("%032x", bigInt);
            return checksum;
        } catch (final IOException e) {
            return null;
        } catch (final NoSuchAlgorithmException e) {
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (final IOException e) {
                return null;
            }
        }
    }

    @Override
    public String downloadPublicTemplate(final long id, final String url, final String name, final ImageFormat format, final boolean hvm, final Long accountId, final String
            descr, final String cksum,
                                         final String installPathPrefix, final String templatePath, final String user, final String password, final long maxTemplateSizeInBytes,
                                         final Proxy proxy, final ResourceType
                                                 resourceType) {
        final UUID uuid = UUID.randomUUID();
        final String jobId = uuid.toString();
        final String tmpDir = installPathPrefix;

        try {

            if (!_storage.mkdirs(tmpDir)) {
                s_logger.warn("Unable to create " + tmpDir);
                return "Unable to create " + tmpDir;
            }
            // TO DO - define constant for volume properties.
            final File file =
                    ResourceType.TEMPLATE == resourceType ? _storage.getFile(tmpDir + File.separator + TemplateLocation.Filename) : _storage.getFile(tmpDir + File.separator +
                            "volume.properties");
            if (file.exists()) {
                if (!file.delete()) {
                    s_logger.warn("Deletion of file '" + file.getAbsolutePath() + "' failed.");
                }
            }

            if (!file.createNewFile()) {
                s_logger.warn("Unable to create new file: " + file.getAbsolutePath());
                return "Unable to create new file: " + file.getAbsolutePath();
            }

            final URI uri;
            try {
                uri = new URI(url);
            } catch (final URISyntaxException e) {
                throw new CloudRuntimeException("URI is incorrect: " + url);
            }
            final TemplateDownloader td;
            if (uri != null && uri.getScheme() != null) {
                if (uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https")) {
                    td = new HttpTemplateDownloader(_storage, url, tmpDir, new Completion(jobId), maxTemplateSizeInBytes, user, password, proxy, resourceType);
                } else if (uri.getScheme().equalsIgnoreCase("file")) {
                    td = new LocalTemplateDownloader(_storage, url, tmpDir, maxTemplateSizeInBytes, new Completion(jobId));
                } else if (uri.getScheme().equalsIgnoreCase("scp")) {
                    td = new ScpTemplateDownloader(_storage, url, tmpDir, maxTemplateSizeInBytes, new Completion(jobId));
                } else if (uri.getScheme().equalsIgnoreCase("nfs") || uri.getScheme().equalsIgnoreCase("cifs")) {
                    td = null;
                    // TODO: implement this.
                    throw new CloudRuntimeException("Scheme is not supported " + url);
                } else {
                    throw new CloudRuntimeException("Scheme is not supported " + url);
                }
            } else {
                throw new CloudRuntimeException("Unable to download from URL: " + url);
            }
            // NOTE the difference between installPathPrefix and templatePath
            // here. instalPathPrefix is the absolute path for template
            // including mount directory
            // on ssvm, while templatePath is the final relative path on
            // secondary storage.
            final DownloadJob dj = new DownloadJob(td, jobId, id, name, format, hvm, accountId, descr, cksum, installPathPrefix, resourceType);
            dj.setTmpltPath(templatePath);
            jobs.put(jobId, dj);
            threadPool.execute(td);

            return jobId;
        } catch (final IOException e) {
            s_logger.warn("Unable to download to " + tmpDir, e);
            return null;
        }
    }

    @Override
    public Map<String, Processor> getProcessors() {
        return _processors;
    }

    @Override
    public Status getDownloadStatus(final String jobId) {
        final DownloadJob job = jobs.get(jobId);
        if (job != null) {
            final TemplateDownloader td = job.getTemplateDownloader();
            if (td != null) {
                return td.getStatus();
            }
        }
        return Status.UNKNOWN;
    }

    @Override
    public com.cloud.storage.VMTemplateHostVO.Status getDownloadStatus2(final String jobId) {
        return convertStatus(getDownloadStatus(jobId));
    }

    @Override
    public int getDownloadPct(final String jobId) {
        final DownloadJob dj = jobs.get(jobId);
        if (dj != null) {
            return dj.getTemplateDownloader().getDownloadPercent();
        }
        return 0;
    }

    @Override
    public String getDownloadError(final String jobId) {
        final DownloadJob dj = jobs.get(jobId);
        if (dj != null) {
            return dj.getTemplateDownloader().getDownloadError();
        }
        return null;
    }

    @Override
    public DownloadAnswer handleDownloadCommand(final SecondaryStorageResource resource, final DownloadCommand cmd) {
        final ResourceType resourceType = cmd.getResourceType();
        if (cmd instanceof DownloadProgressCommand) {
            return handleDownloadProgressCmd(resource, (DownloadProgressCommand) cmd);
        }

        if (cmd.getUrl() == null) {
            return new DownloadAnswer(resourceType.toString() + " is corrupted on storage due to an invalid url , cannot download",
                    VMTemplateStorageResourceAssoc.Status.DOWNLOAD_ERROR);
        }

        if (cmd.getName() == null) {
            return new DownloadAnswer("Invalid Name", VMTemplateStorageResourceAssoc.Status.DOWNLOAD_ERROR);
        }

        final DataStoreTO dstore = cmd.getDataStore();
        String installPathPrefix = cmd.getInstallPath();
        // for NFS, we need to get mounted path
        if (dstore instanceof NfsTO) {
            installPathPrefix = resource.getRootDir(((NfsTO) dstore).getUrl()) + File.separator + installPathPrefix;
        }
        String user = null;
        String password = null;
        if (cmd.getAuth() != null) {
            user = cmd.getAuth().getUserName();
            password = cmd.getAuth().getPassword();
        }
        // TO DO - Define Volume max size as well
        final long maxDownloadSizeInBytes =
                cmd.getMaxDownloadSizeInBytes() == null ? TemplateDownloader.DEFAULT_MAX_TEMPLATE_SIZE_IN_BYTES : cmd.getMaxDownloadSizeInBytes();
        String jobId = null;
        jobId =
                downloadPublicTemplate(cmd.getId(), cmd.getUrl(), cmd.getName(), cmd.getFormat(), cmd.isHvm(), cmd.getAccountId(), cmd.getDescription(),
                        cmd.getChecksum(), installPathPrefix, cmd.getInstallPath(), user, password, maxDownloadSizeInBytes, cmd.getProxy(), resourceType);
        sleep();
        if (jobId == null) {
            return new DownloadAnswer("Internal Error", VMTemplateStorageResourceAssoc.Status.DOWNLOAD_ERROR);
        }
        return new DownloadAnswer(jobId, getDownloadPct(jobId), getDownloadError(jobId), getDownloadStatus2(jobId), getDownloadLocalPath(jobId), getInstallPath(jobId),
                getDownloadTemplateSize(jobId), getDownloadTemplateSize(jobId), getDownloadCheckSum(jobId));
    }

    @Override
    public Map<String, TemplateProp> gatherTemplateInfo(final String rootDir) {
        final Map<String, TemplateProp> result = new HashMap<>();
        final String templateDir = rootDir + File.separator + _templateDir;

        if (!_storage.exists(templateDir)) {
            _storage.mkdirs(templateDir);
        }

        final List<String> publicTmplts = listTemplates(templateDir);
        for (final String tmplt : publicTmplts) {
            final String path = tmplt.substring(0, tmplt.lastIndexOf(File.separator));
            final TemplateLocation loc = new TemplateLocation(_storage, path);
            try {
                if (!loc.load()) {
                    s_logger.warn("Post download installation was not completed for " + path);
                    // loc.purge();
                    _storage.cleanup(path, templateDir);
                    continue;
                }
            } catch (final IOException e) {
                s_logger.warn("Unable to load template location " + path, e);
                continue;
            }

            final TemplateProp tInfo = loc.getTemplateInfo();

            result.put(tInfo.getTemplateName(), tInfo);
            s_logger.debug("Added template name: " + tInfo.getTemplateName() + ", path: " + tmplt);
        }
    /*
        for (String tmplt : isoTmplts) {
            String tmp[];
            tmp = tmplt.split("/");
            String tmpltName = tmp[tmp.length - 2];
            tmplt = tmplt.substring(tmplt.lastIndexOf("iso/"));
            TemplateInfo tInfo = new TemplateInfo(tmpltName, tmplt, false);
            s_logger.debug("Added iso template name: " + tmpltName + ", path: " + tmplt);
            result.put(tmpltName, tInfo);
        }
     */
        return result;
    }

    private List<String> listTemplates(final String rootdir) {
        final List<String> result = new ArrayList<>();

        final Script script = new Script(listTmpltScr, s_logger);
        script.add("-r", rootdir);
        final ZfsPathParser zpp = new ZfsPathParser(rootdir);
        script.execute(zpp);
        result.addAll(zpp.getPaths());
        s_logger.info("found " + zpp.getPaths().size() + " templates" + zpp.getPaths());
        return result;
    }

    @Override
    public Map<Long, TemplateProp> gatherVolumeInfo(final String rootDir) {
        final Map<Long, TemplateProp> result = new HashMap<>();
        final String volumeDir = rootDir + File.separator + _volumeDir;

        if (!_storage.exists(volumeDir)) {
            _storage.mkdirs(volumeDir);
        }

        final List<String> vols = listVolumes(volumeDir);
        for (final String vol : vols) {
            final String path = vol.substring(0, vol.lastIndexOf(File.separator));
            final TemplateLocation loc = new TemplateLocation(_storage, path);
            try {
                if (!loc.load()) {
                    s_logger.warn("Post download installation was not completed for " + path);
                    // loc.purge();
                    _storage.cleanup(path, volumeDir);
                    continue;
                }
            } catch (final IOException e) {
                s_logger.warn("Unable to load volume location " + path, e);
                continue;
            }

            final TemplateProp vInfo = loc.getTemplateInfo();

            result.put(vInfo.getId(), vInfo);
            s_logger.debug("Added volume name: " + vInfo.getTemplateName() + ", path: " + vol);
        }
        return result;
    }

    private List<String> listVolumes(final String rootdir) {
        final List<String> result = new ArrayList<>();

        final Script script = new Script(listVolScr, s_logger);
        script.add("-r", rootdir);
        final ZfsPathParser zpp = new ZfsPathParser(rootdir);
        script.execute(zpp);
        result.addAll(zpp.getPaths());
        s_logger.info("found " + zpp.getPaths().size() + " volumes" + zpp.getPaths());
        return result;
    }

    public long getDownloadTemplateSize(final String jobId) {
        final DownloadJob dj = jobs.get(jobId);
        if (dj != null) {
            return dj.getTemplatesize();
        }
        return 0;
    }

    public String getDownloadCheckSum(final String jobId) {
        final DownloadJob dj = jobs.get(jobId);
        if (dj != null) {
            return dj.getChecksum();
        }
        return null;
    }

    public long getDownloadTemplatePhysicalSize(final String jobId) {
        final DownloadJob dj = jobs.get(jobId);
        if (dj != null) {
            return dj.getTemplatePhysicalSize();
        }
        return 0;
    }

    // @Override
    public String getDownloadLocalPath(final String jobId) {
        final DownloadJob dj = jobs.get(jobId);
        if (dj != null) {
            return dj.getTemplateDownloader().getDownloadLocalPath();
        }
        return null;
    }

    private void sleep() {
        try {
            Thread.sleep(3000);
        } catch (final InterruptedException e) {
            // ignore
        }
    }

    private DownloadAnswer handleDownloadProgressCmd(final SecondaryStorageResource resource, final DownloadProgressCommand cmd) {
        final String jobId = cmd.getJobId();
        final DownloadAnswer answer;
        DownloadJob dj = null;
        if (jobId != null) {
            dj = jobs.get(jobId);
        }
        if (dj == null) {
            if (cmd.getRequest() == RequestType.GET_OR_RESTART) {
                final DownloadCommand dcmd = new DownloadCommand(cmd);
                return handleDownloadCommand(resource, dcmd);
            } else {
                return new DownloadAnswer("Cannot find job", com.cloud.storage.VMTemplateStorageResourceAssoc.Status.UNKNOWN);
            }
        }
        final TemplateDownloader td = dj.getTemplateDownloader();
        switch (cmd.getRequest()) {
            case GET_STATUS:
                break;
            case ABORT:
                td.stopDownload();
                sleep();
                break;
            case RESTART:
                td.stopDownload();
                sleep();
                threadPool.execute(td);
                break;
            case PURGE:
                td.stopDownload();
                answer =
                        new DownloadAnswer(jobId, getDownloadPct(jobId), getDownloadError(jobId), getDownloadStatus2(jobId), getDownloadLocalPath(jobId),
                                getInstallPath(jobId), getDownloadTemplateSize(jobId), getDownloadTemplatePhysicalSize(jobId), getDownloadCheckSum(jobId));
                jobs.remove(jobId);
                return answer;
            default:
                break; // TODO
        }
        return new DownloadAnswer(jobId, getDownloadPct(jobId), getDownloadError(jobId), getDownloadStatus2(jobId), getDownloadLocalPath(jobId), getInstallPath(jobId),
                getDownloadTemplateSize(jobId), getDownloadTemplatePhysicalSize(jobId), getDownloadCheckSum(jobId));
    }

    private String getInstallPath(final String jobId) {
        final DownloadJob dj = jobs.get(jobId);
        if (dj != null) {
            return dj.getTmpltPath();
        }
        return null;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;

        String value = null;

        _storage = (StorageLayer) params.get(StorageLayer.InstanceConfigKey);
        if (_storage == null) {
            value = (String) params.get(StorageLayer.ClassConfigKey);
            if (value == null) {
                throw new ConfigurationException("Unable to find the storage layer");
            }

            final Class<StorageLayer> clazz;
            try {
                clazz = (Class<StorageLayer>) Class.forName(value);
                _storage = clazz.newInstance();
            } catch (final ClassNotFoundException e) {
                throw new ConfigurationException("Unable to instantiate " + value);
            } catch (final InstantiationException e) {
                throw new ConfigurationException("Unable to instantiate " + value);
            } catch (final IllegalAccessException e) {
                throw new ConfigurationException("Unable to instantiate " + value);
            }
        }

        final String inSystemVM = (String) params.get("secondary.storage.vm");
        if (inSystemVM != null && "true".equalsIgnoreCase(inSystemVM)) {
            s_logger.info("DownloadManager: starting additional services since we are inside system vm");
            startAdditionalServices();
            blockOutgoingOnPrivate();
        }

        value = (String) params.get("install.timeout.pergig");
        installTimeoutPerGig = NumbersUtil.parseInt(value, 15 * 60) * 1000;

        value = (String) params.get("install.numthreads");
        final int numInstallThreads = NumbersUtil.parseInt(value, 10);

        String scriptsDir = (String) params.get("template.scripts.dir");
        if (scriptsDir == null) {
            scriptsDir = "scripts/storage/secondary";
        }

        listTmpltScr = Script.findScript(scriptsDir, "listvmtmplt.sh");
        if (listTmpltScr == null) {
            throw new ConfigurationException("Unable to find the listvmtmplt.sh");
        }
        s_logger.info("listvmtmplt.sh found in " + listTmpltScr);

        createTmpltScr = Script.findScript(scriptsDir, "createtmplt.sh");
        if (createTmpltScr == null) {
            throw new ConfigurationException("Unable to find createtmplt.sh");
        }
        s_logger.info("createtmplt.sh found in " + createTmpltScr);

        listVolScr = Script.findScript(scriptsDir, "listvolume.sh");
        if (listVolScr == null) {
            throw new ConfigurationException("Unable to find the listvolume.sh");
        }
        s_logger.info("listvolume.sh found in " + listVolScr);

        createVolScr = Script.findScript(scriptsDir, "createvolume.sh");
        if (createVolScr == null) {
            throw new ConfigurationException("Unable to find createvolume.sh");
        }
        s_logger.info("createvolume.sh found in " + createVolScr);

        _processors = new HashMap<>();

        Processor processor = new VhdProcessor();
        processor.configure("VHD Processor", params);
        _processors.put("VHD Processor", processor);

        processor = new IsoProcessor();
        processor.configure("ISO Processor", params);
        _processors.put("ISO Processor", processor);

        processor = new QCOW2Processor();
        processor.configure("QCOW2 Processor", params);
        _processors.put("QCOW2 Processor", processor);

        processor = new RawImageProcessor();
        processor.configure("Raw Image Processor", params);
        _processors.put("Raw Image Processor", processor);

        processor = new TARProcessor();
        processor.configure("TAR Processor", params);
        _processors.put("TAR Processor", processor);

        _templateDir = (String) params.get("public.templates.root.dir");
        if (_templateDir == null) {
            _templateDir = TemplateConstants.DEFAULT_TMPLT_ROOT_DIR;
        }
        _templateDir += File.separator + TemplateConstants.DEFAULT_TMPLT_FIRST_LEVEL_DIR;
        _volumeDir = TemplateConstants.DEFAULT_VOLUME_ROOT_DIR + File.separator;
        // Add more processors here.
        threadPool = Executors.newFixedThreadPool(numInstallThreads);
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    private void startAdditionalServices() {

        Script command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("if [ -d /etc/apache2 ] ; then service apache2 stop; else service httpd stop; fi ");
        String result = command.execute();
        if (result != null) {
            s_logger.warn("Error in stopping httpd service err=" + result);
        }

        command = new Script("/bin/bash", s_logger);
        command.add("-c");
        command.add("if [ -d /etc/apache2 ] ; then service apache2 start; else service httpd start; fi ");
        result = command.execute();
        if (result != null) {
            s_logger.warn("Error in starting httpd service err=" + result);
            return;
        }
        command = new Script("mkdir", s_logger);
        command.add("-p");
        command.add("/var/www/html/copy/template");
        result = command.execute();
        if (result != null) {
            s_logger.warn("Error in creating directory =" + result);
            return;
        }
    }

    private void blockOutgoingOnPrivate() {
        final Script command = new Script("/bin/bash", s_logger);
        final String intf = "eth1";
        command.add("-c");
        command.add("iptables -A OUTPUT -o " + intf + " -p tcp -m state --state NEW -m tcp --dport " + "80" + " -j REJECT;" + "iptables -A OUTPUT -o " + intf +
                " -p tcp -m state --state NEW -m tcp --dport " + "443" + " -j REJECT;");

        final String result = command.execute();
        if (result != null) {
            s_logger.warn("Error in blocking outgoing to port 80/443 err=" + result);
            return;
        }
    }

    private static class DownloadJob {
        private final TemplateDownloader td;
        private final String tmpltName;
        private final boolean hvm;
        private final ImageFormat format;
        private final String description;
        private final String installPathPrefix;
        private final long id;
        private final ResourceType resourceType;
        private String tmpltPath;
        private String checksum;
        private long templatesize;
        private long templatePhysicalSize;

        public DownloadJob(final TemplateDownloader td, final String jobId, final long id, final String tmpltName, final ImageFormat format, final boolean hvm, final Long
                accountId, final String descr, final String cksum,
                           final String installPathPrefix, final ResourceType resourceType) {
            super();
            this.td = td;
            this.tmpltName = tmpltName;
            this.format = format;
            this.hvm = hvm;
            description = descr;
            checksum = cksum;
            this.installPathPrefix = installPathPrefix;
            templatesize = 0;
            this.id = id;
            this.resourceType = resourceType;
        }

        public String getDescription() {
            return description;
        }

        public String getChecksum() {
            return checksum;
        }

        public TemplateDownloader getTemplateDownloader() {
            return td;
        }

        public String getTmpltName() {
            return tmpltName;
        }

        public ImageFormat getFormat() {
            return format;
        }

        public boolean isHvm() {
            return hvm;
        }

        public long getId() {
            return id;
        }

        public ResourceType getResourceType() {
            return resourceType;
        }

        public String getTmpltPath() {
            return tmpltPath;
        }

        public void setTmpltPath(final String tmpltPath) {
            this.tmpltPath = tmpltPath;
        }

        public String getInstallPathPrefix() {
            return installPathPrefix;
        }

        public void cleanup() {
            if (td != null) {
                final String dnldPath = td.getDownloadLocalPath();
                if (dnldPath != null) {
                    final File f = new File(dnldPath);
                    final File dir = f.getParentFile();
                    f.delete();
                    if (dir != null) {
                        dir.delete();
                    }
                }
            }
        }

        public long getTemplatesize() {
            return templatesize;
        }

        public void setTemplatesize(final long templatesize) {
            this.templatesize = templatesize;
        }

        public long getTemplatePhysicalSize() {
            return templatePhysicalSize;
        }

        public void setTemplatePhysicalSize(final long templatePhysicalSize) {
            this.templatePhysicalSize = templatePhysicalSize;
        }

        public void setCheckSum(final String checksum) {
            this.checksum = checksum;
        }
    }

    public static class ZfsPathParser extends OutputInterpreter {
        String _parent;
        List<String> paths = new ArrayList<>();

        public ZfsPathParser(final String parent) {
            _parent = parent;
        }

        public List<String> getPaths() {
            return paths;
        }

        @Override
        public boolean drain() {
            return true;
        }

        @Override
        public String interpret(final BufferedReader reader) throws IOException {
            String line = null;
            while ((line = reader.readLine()) != null) {
                paths.add(line);
            }
            return null;
        }
    }

    public class Completion implements DownloadCompleteCallback {
        private final String jobId;

        public Completion(final String jobId) {
            this.jobId = jobId;
        }

        @Override
        public void downloadComplete(final Status status) {
            setDownloadStatus(jobId, status);
        }
    }
}
