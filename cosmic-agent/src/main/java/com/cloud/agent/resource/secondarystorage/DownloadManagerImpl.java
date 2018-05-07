package com.cloud.agent.resource.secondarystorage;

import com.cloud.common.storageprocessor.IsoProcessor;
import com.cloud.common.storageprocessor.Processor;
import com.cloud.common.storageprocessor.QCOW2Processor;
import com.cloud.common.storageprocessor.RawImageProcessor;
import com.cloud.common.storageprocessor.TARProcessor;
import com.cloud.common.storageprocessor.TemplateConstants;
import com.cloud.common.storageprocessor.TemplateLocation;
import com.cloud.common.storageprocessor.VhdProcessor;
import com.cloud.legacymodel.communication.answer.DownloadAnswer;
import com.cloud.legacymodel.communication.command.DownloadCommand;
import com.cloud.legacymodel.communication.command.DownloadCommand.ResourceType;
import com.cloud.legacymodel.communication.command.DownloadProgressCommand;
import com.cloud.legacymodel.communication.command.DownloadProgressCommand.RequestType;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.InternalErrorException;
import com.cloud.legacymodel.network.Proxy;
import com.cloud.legacymodel.storage.DownloadCompleteCallback;
import com.cloud.legacymodel.storage.TemplateDownloadStatus;
import com.cloud.legacymodel.storage.TemplateFormatInfo;
import com.cloud.legacymodel.storage.TemplateProp;
import com.cloud.legacymodel.storage.VMTemplateStatus;
import com.cloud.legacymodel.to.DataStoreTO;
import com.cloud.legacymodel.to.NfsTO;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.StringUtils;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;
import com.cloud.utils.storage.StorageLayer;

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

    public static VMTemplateStatus convertStatus(final TemplateDownloadStatus tds) {
        switch (tds) {
            case ABORTED:
                return VMTemplateStatus.NOT_DOWNLOADED;
            case DOWNLOAD_FINISHED:
                return VMTemplateStatus.DOWNLOAD_IN_PROGRESS;
            case IN_PROGRESS:
                return VMTemplateStatus.DOWNLOAD_IN_PROGRESS;
            case NOT_STARTED:
                return VMTemplateStatus.NOT_DOWNLOADED;
            case RECOVERABLE_ERROR:
                return VMTemplateStatus.NOT_DOWNLOADED;
            case UNKNOWN:
                return VMTemplateStatus.UNKNOWN;
            case UNRECOVERABLE_ERROR:
                return VMTemplateStatus.DOWNLOAD_ERROR;
            case POST_DOWNLOAD_FINISHED:
                return VMTemplateStatus.DOWNLOADED;
            default:
                return VMTemplateStatus.UNKNOWN;
        }
    }

    public void setThreadPool(final ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public void setStorageLayer(final StorageLayer storage) {
        this._storage = storage;
    }

    /**
     * Get notified of change of job status. Executed in context of downloader
     * thread
     *
     * @param jobId  the id of the job
     * @param status the status of the job
     */
    public void setDownloadStatus(final String jobId, final TemplateDownloadStatus status) {
        final DownloadJob dj = this.jobs.get(jobId);
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
                this.threadPool.execute(td);
                break;
            case RECOVERABLE_ERROR:
                this.threadPool.execute(td);
                break;
            case DOWNLOAD_FINISHED:
                // For other TemplateDownloaders where files are locally available,
                // we run the postLocalDownload() method.
                td.setDownloadError("Download success, starting install ");
                final String result = postLocalDownload(jobId);
                if (result != null) {
                    s_logger.error("Failed post download script: " + result);
                    td.setStatus(TemplateDownloadStatus.UNRECOVERABLE_ERROR);
                    td.setDownloadError("Failed post download script: " + result);
                } else {
                    td.setStatus(TemplateDownloadStatus.POST_DOWNLOAD_FINISHED);
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
        final DownloadJob dnld = this.jobs.get(jobId);
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

        int imgSizeGigs = (int) Math.ceil(this._storage.getSize(td.getDownloadLocalPath()) * 1.0d / (1024 * 1024 * 1024));
        imgSizeGigs++; // add one just in case
        final long timeout = (long) imgSizeGigs * this.installTimeoutPerGig;
        Script scr = null;
        final String script = resourceType == ResourceType.TEMPLATE ? this.createTmpltScr : this.createVolScr;
        scr = new Script(script, timeout, s_logger);
        scr.add("-s", Integer.toString(imgSizeGigs));
        scr.add("-S", Long.toString(td.getMaxTemplateSizeInBytes()));
        if (dnld.getDescription() != null && dnld.getDescription().length() > 1) {
            scr.add("-d", dnld.getDescription());
        }
        // add options common to ISO and template
        final String extension = dnld.getFormat().getFileExtension();
        String templateName = "";
        if (extension.equals("iso")) {
            templateName = this.jobs.get(jobId).getTmpltName().trim().replace(" ", "_");
        } else {
            templateName = java.util.UUID.nameUUIDFromBytes((this.jobs.get(jobId).getTmpltName() + System.currentTimeMillis()).getBytes(StringUtils.getPreferredCharset())).toString();
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
        this._storage.setWorldReadableAndWriteable(downloadedTemplate);

        // Set permissions for template/volume.properties
        String propertiesFile = resourcePath;
        if (resourceType == ResourceType.TEMPLATE) {
            propertiesFile += "/template.properties";
        } else {
            propertiesFile += "/volume.properties";
        }
        final File templateProperties = new File(propertiesFile);
        this._storage.setWorldReadableAndWriteable(templateProperties);

        final TemplateLocation loc = new TemplateLocation(this._storage, resourcePath);
        try {
            loc.create(dnld.getId(), true, dnld.getTmpltName());
        } catch (final IOException e) {
            s_logger.warn("Something is wrong with template location " + resourcePath, e);
            loc.purge();
            return "Unable to download due to " + e.getMessage();
        }

        final Iterator<Processor> en = this._processors.values().iterator();
        while (en.hasNext()) {
            final Processor processor = en.next();

            TemplateFormatInfo info = null;
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
    public String downloadPublicTemplate(final long id, final String url, final String name, final ImageFormat format, final Long accountId, final String descr, final String cksum,
                                         final String installPathPrefix, final String templatePath, final String user, final String password, final long maxTemplateSizeInBytes,
                                         final Proxy proxy, final ResourceType resourceType) {
        final UUID uuid = UUID.randomUUID();
        final String jobId = uuid.toString();
        final String tmpDir = installPathPrefix;

        try {

            if (!this._storage.mkdirs(tmpDir)) {
                s_logger.warn("Unable to create " + tmpDir);
                return "Unable to create " + tmpDir;
            }
            // TO DO - define constant for volume properties.
            final File file =
                    ResourceType.TEMPLATE == resourceType ? this._storage.getFile(tmpDir + File.separator + TemplateLocation.Filename) : this._storage.getFile(tmpDir + File.separator +
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
                    td = new HttpTemplateDownloader(this._storage, url, tmpDir, new Completion(jobId), maxTemplateSizeInBytes, user, password, proxy, resourceType);
                } else if (uri.getScheme().equalsIgnoreCase("file")) {
                    td = new LocalTemplateDownloader(this._storage, url, tmpDir, maxTemplateSizeInBytes, new Completion(jobId));
                } else if (uri.getScheme().equalsIgnoreCase("scp")) {
                    td = new ScpTemplateDownloader(this._storage, url, tmpDir, maxTemplateSizeInBytes, new Completion(jobId));
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
            final DownloadJob dj = new DownloadJob(td, jobId, id, name, format, accountId, descr, cksum, installPathPrefix, resourceType);
            dj.setTmpltPath(templatePath);
            this.jobs.put(jobId, dj);
            this.threadPool.execute(td);

            return jobId;
        } catch (final IOException e) {
            s_logger.warn("Unable to download to " + tmpDir, e);
            return null;
        }
    }

    @Override
    public Map<String, Processor> getProcessors() {
        return this._processors;
    }

    @Override
    public TemplateDownloadStatus getDownloadStatus(final String jobId) {
        final DownloadJob job = this.jobs.get(jobId);
        if (job != null) {
            final TemplateDownloader td = job.getTemplateDownloader();
            if (td != null) {
                return td.getStatus();
            }
        }
        return TemplateDownloadStatus.UNKNOWN;
    }

    @Override
    public VMTemplateStatus getDownloadStatus2(final String jobId) {
        return convertStatus(getDownloadStatus(jobId));
    }

    @Override
    public int getDownloadPct(final String jobId) {
        final DownloadJob dj = this.jobs.get(jobId);
        if (dj != null) {
            return dj.getTemplateDownloader().getDownloadPercent();
        }
        return 0;
    }

    @Override
    public String getDownloadError(final String jobId) {
        final DownloadJob dj = this.jobs.get(jobId);
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
                    VMTemplateStatus.DOWNLOAD_ERROR);
        }

        if (cmd.getName() == null) {
            return new DownloadAnswer("Invalid Name", VMTemplateStatus.DOWNLOAD_ERROR);
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
                downloadPublicTemplate(cmd.getId(), cmd.getUrl(), cmd.getName(), cmd.getFormat(), cmd.getAccountId(), cmd.getDescription(),
                        cmd.getChecksum(), installPathPrefix, cmd.getInstallPath(), user, password, maxDownloadSizeInBytes, cmd.getProxy(), resourceType);
        sleep();
        if (jobId == null) {
            return new DownloadAnswer("Internal Error", VMTemplateStatus.DOWNLOAD_ERROR);
        }
        return new DownloadAnswer(jobId, getDownloadPct(jobId), getDownloadError(jobId), getDownloadStatus2(jobId), getDownloadLocalPath(jobId), getInstallPath(jobId),
                getDownloadTemplateSize(jobId), getDownloadTemplateSize(jobId), getDownloadCheckSum(jobId));
    }

    @Override
    public Map<String, TemplateProp> gatherTemplateInfo(final String rootDir) {
        final Map<String, TemplateProp> result = new HashMap<>();
        final String templateDir = rootDir + File.separator + this._templateDir;

        if (!this._storage.exists(templateDir)) {
            this._storage.mkdirs(templateDir);
        }

        final List<String> publicTmplts = listTemplates(templateDir);
        for (final String tmplt : publicTmplts) {
            final String path = tmplt.substring(0, tmplt.lastIndexOf(File.separator));
            final TemplateLocation loc = new TemplateLocation(this._storage, path);
            try {
                if (!loc.load()) {
                    s_logger.warn("Post download installation was not completed for " + path);
                    // loc.purge();
                    this._storage.cleanup(path, templateDir);
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

        final Script script = new Script(this.listTmpltScr, s_logger);
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
        final String volumeDir = rootDir + File.separator + this._volumeDir;

        if (!this._storage.exists(volumeDir)) {
            this._storage.mkdirs(volumeDir);
        }

        final List<String> vols = listVolumes(volumeDir);
        for (final String vol : vols) {
            final String path = vol.substring(0, vol.lastIndexOf(File.separator));
            final TemplateLocation loc = new TemplateLocation(this._storage, path);
            try {
                if (!loc.load()) {
                    s_logger.warn("Post download installation was not completed for " + path);
                    // loc.purge();
                    this._storage.cleanup(path, volumeDir);
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

        final Script script = new Script(this.listVolScr, s_logger);
        script.add("-r", rootdir);
        final ZfsPathParser zpp = new ZfsPathParser(rootdir);
        script.execute(zpp);
        result.addAll(zpp.getPaths());
        s_logger.info("found " + zpp.getPaths().size() + " volumes" + zpp.getPaths());
        return result;
    }

    public long getDownloadTemplateSize(final String jobId) {
        final DownloadJob dj = this.jobs.get(jobId);
        if (dj != null) {
            return dj.getTemplatesize();
        }
        return 0;
    }

    public String getDownloadCheckSum(final String jobId) {
        final DownloadJob dj = this.jobs.get(jobId);
        if (dj != null) {
            return dj.getChecksum();
        }
        return null;
    }

    public long getDownloadTemplatePhysicalSize(final String jobId) {
        final DownloadJob dj = this.jobs.get(jobId);
        if (dj != null) {
            return dj.getTemplatePhysicalSize();
        }
        return 0;
    }

    // @Override
    public String getDownloadLocalPath(final String jobId) {
        final DownloadJob dj = this.jobs.get(jobId);
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
            dj = this.jobs.get(jobId);
        }
        if (dj == null) {
            if (cmd.getRequest() == RequestType.GET_OR_RESTART) {
                final DownloadCommand dcmd = new DownloadCommand(cmd);
                return handleDownloadCommand(resource, dcmd);
            } else {
                return new DownloadAnswer("Cannot find job", VMTemplateStatus.UNKNOWN);
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
                this.threadPool.execute(td);
                break;
            case PURGE:
                td.stopDownload();
                answer =
                        new DownloadAnswer(jobId, getDownloadPct(jobId), getDownloadError(jobId), getDownloadStatus2(jobId), getDownloadLocalPath(jobId),
                                getInstallPath(jobId), getDownloadTemplateSize(jobId), getDownloadTemplatePhysicalSize(jobId), getDownloadCheckSum(jobId));
                this.jobs.remove(jobId);
                return answer;
            default:
                break; // TODO
        }
        return new DownloadAnswer(jobId, getDownloadPct(jobId), getDownloadError(jobId), getDownloadStatus2(jobId), getDownloadLocalPath(jobId), getInstallPath(jobId),
                getDownloadTemplateSize(jobId), getDownloadTemplatePhysicalSize(jobId), getDownloadCheckSum(jobId));
    }

    private String getInstallPath(final String jobId) {
        final DownloadJob dj = this.jobs.get(jobId);
        if (dj != null) {
            return dj.getTmpltPath();
        }
        return null;
    }

    @Override
    public String getName() {
        return this._name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        this._name = name;

        String value = null;

        this._storage = (StorageLayer) params.get(StorageLayer.InstanceConfigKey);
        if (this._storage == null) {
            value = (String) params.get(StorageLayer.ClassConfigKey);
            if (value == null) {
                throw new ConfigurationException("Unable to find the storage layer");
            }

            final Class<StorageLayer> clazz;
            try {
                clazz = (Class<StorageLayer>) Class.forName(value);
                this._storage = clazz.newInstance();
            } catch (final ClassNotFoundException e) {
                throw new ConfigurationException("Unable to instantiate " + value);
            } catch (final InstantiationException e) {
                throw new ConfigurationException("Unable to instantiate " + value);
            } catch (final IllegalAccessException e) {
                throw new ConfigurationException("Unable to instantiate " + value);
            }
        }

        value = (String) params.get("install.timeout.pergig");
        this.installTimeoutPerGig = NumbersUtil.parseInt(value, 15 * 60) * 1000;

        value = (String) params.get("install.numthreads");
        final int numInstallThreads = NumbersUtil.parseInt(value, 10);

        String scriptsDir = (String) params.get("template.scripts.dir");
        if (scriptsDir == null) {
            scriptsDir = "scripts/storage/secondary";
        }

        this.listTmpltScr = Script.findScript(scriptsDir, "listvmtmplt.sh");
        if (this.listTmpltScr == null) {
            throw new ConfigurationException("Unable to find the listvmtmplt.sh");
        }
        s_logger.info("listvmtmplt.sh found in " + this.listTmpltScr);

        this.createTmpltScr = Script.findScript(scriptsDir, "createtmplt.sh");
        if (this.createTmpltScr == null) {
            throw new ConfigurationException("Unable to find createtmplt.sh");
        }
        s_logger.info("createtmplt.sh found in " + this.createTmpltScr);

        this.listVolScr = Script.findScript(scriptsDir, "listvolume.sh");
        if (this.listVolScr == null) {
            throw new ConfigurationException("Unable to find the listvolume.sh");
        }
        s_logger.info("listvolume.sh found in " + this.listVolScr);

        this.createVolScr = Script.findScript(scriptsDir, "createvolume.sh");
        if (this.createVolScr == null) {
            throw new ConfigurationException("Unable to find createvolume.sh");
        }
        s_logger.info("createvolume.sh found in " + this.createVolScr);

        this._processors = new HashMap<>();

        Processor processor = new VhdProcessor();
        processor.configure("VHD Processor", params);
        this._processors.put("VHD Processor", processor);

        processor = new IsoProcessor();
        processor.configure("ISO Processor", params);
        this._processors.put("ISO Processor", processor);

        processor = new QCOW2Processor();
        processor.configure("QCOW2 Processor", params);
        this._processors.put("QCOW2 Processor", processor);

        processor = new RawImageProcessor();
        processor.configure("Raw Image Processor", params);
        this._processors.put("Raw Image Processor", processor);

        processor = new TARProcessor();
        processor.configure("TAR Processor", params);
        this._processors.put("TAR Processor", processor);

        this._templateDir = (String) params.get("public.templates.root.dir");
        if (this._templateDir == null) {
            this._templateDir = TemplateConstants.DEFAULT_TMPLT_ROOT_DIR;
        }
        this._templateDir += File.separator + TemplateConstants.DEFAULT_TMPLT_FIRST_LEVEL_DIR;
        this._volumeDir = TemplateConstants.DEFAULT_VOLUME_ROOT_DIR + File.separator;
        // Add more processors here.
        this.threadPool = Executors.newFixedThreadPool(numInstallThreads);
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

    private static class DownloadJob {
        private final TemplateDownloader td;
        private final String tmpltName;
        private final ImageFormat format;
        private final String description;
        private final String installPathPrefix;
        private final long id;
        private final ResourceType resourceType;
        private String tmpltPath;
        private String checksum;
        private long templatesize;
        private long templatePhysicalSize;

        public DownloadJob(final TemplateDownloader td, final String jobId, final long id, final String tmpltName, final ImageFormat format, final Long accountId,
                           final String descr, final String cksum, final String installPathPrefix, final ResourceType resourceType) {
            super();
            this.td = td;
            this.tmpltName = tmpltName;
            this.format = format;
            this.description = descr;
            this.checksum = cksum;
            this.installPathPrefix = installPathPrefix;
            this.templatesize = 0;
            this.id = id;
            this.resourceType = resourceType;
        }

        public String getDescription() {
            return this.description;
        }

        public String getChecksum() {
            return this.checksum;
        }

        public TemplateDownloader getTemplateDownloader() {
            return this.td;
        }

        public String getTmpltName() {
            return this.tmpltName;
        }

        public ImageFormat getFormat() {
            return this.format;
        }

        public long getId() {
            return this.id;
        }

        public ResourceType getResourceType() {
            return this.resourceType;
        }

        public String getTmpltPath() {
            return this.tmpltPath;
        }

        public void setTmpltPath(final String tmpltPath) {
            this.tmpltPath = tmpltPath;
        }

        public String getInstallPathPrefix() {
            return this.installPathPrefix;
        }

        public void cleanup() {
            if (this.td != null) {
                final String dnldPath = this.td.getDownloadLocalPath();
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
            return this.templatesize;
        }

        public void setTemplatesize(final long templatesize) {
            this.templatesize = templatesize;
        }

        public long getTemplatePhysicalSize() {
            return this.templatePhysicalSize;
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
            this._parent = parent;
        }

        public List<String> getPaths() {
            return this.paths;
        }

        @Override
        public boolean drain() {
            return true;
        }

        @Override
        public String interpret(final BufferedReader reader) throws IOException {
            String line = null;
            while ((line = reader.readLine()) != null) {
                this.paths.add(line);
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
        public void downloadComplete(final TemplateDownloadStatus status) {
            setDownloadStatus(this.jobId, status);
        }
    }
}
