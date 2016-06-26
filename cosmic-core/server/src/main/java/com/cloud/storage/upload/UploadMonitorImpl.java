package com.cloud.storage.upload;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.CreateEntityDownloadURLCommand;
import com.cloud.agent.api.storage.DeleteEntityDownloadURLCommand;
import com.cloud.agent.api.storage.UploadCommand;
import com.cloud.agent.api.storage.UploadProgressCommand.RequestType;
import com.cloud.api.ApiDBUtils;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.resource.ResourceManager;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Upload;
import com.cloud.storage.Upload.Mode;
import com.cloud.storage.Upload.Status;
import com.cloud.storage.Upload.Type;
import com.cloud.storage.UploadVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.UploadDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.SecondaryStorageVm;
import com.cloud.vm.SecondaryStorageVmVO;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.dao.SecondaryStorageVmDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.framework.jobs.AsyncJobManager;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;
import org.apache.cloudstack.storage.datastore.db.ImageStoreVO;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreVO;
import org.apache.cloudstack.storage.image.datastore.ImageStoreEntity;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Monitors the progress of upload.
 */
@Component
public class UploadMonitorImpl extends ManagerBase implements UploadMonitor {

    static final Logger s_logger = LoggerFactory.getLogger(UploadMonitorImpl.class);
    final Map<UploadVO, UploadListener> _listenerMap = new ConcurrentHashMap<>();
    @Inject
    private final HostDao _serverDao = null;
    @Inject
    private UploadDao _uploadDao;
    @Inject
    private SecondaryStorageVmDao _secStorageVmDao;
    @Inject
    private AgentManager _agentMgr;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private ResourceManager _resourceMgr;
    @Inject
    private EndPointSelector _epSelector;
    @Inject
    private DataStoreManager storeMgr;
    private boolean _sslCopy = false;
    private String _ssvmUrlDomain;
    private ScheduledExecutorService _executor = null;
    private Timer _timer;
    private int _cleanupInterval;
    private int _urlExpirationInterval;

    @Override
    public void cancelAllUploads(final Long templateId) {
        // TODO

    }

    @Override
    public Long extractTemplate(final VMTemplateVO template, final String url, final TemplateDataStoreVO vmTemplateHost, final Long dataCenterId, final long eventId, final long
            asyncJobId,
                                final AsyncJobManager asyncMgr) {

        final Type type = (template.getFormat() == ImageFormat.ISO) ? Type.ISO : Type.TEMPLATE;

        final DataStore secStore = storeMgr.getImageStore(dataCenterId);

        final UploadVO uploadTemplateObj = new UploadVO(secStore.getId(), template.getId(), new Date(), Upload.Status.NOT_UPLOADED, type, url, Mode.FTP_UPLOAD);
        _uploadDao.persist(uploadTemplateObj);

        if (vmTemplateHost != null) {
            start();
            final UploadCommand ucmd = new UploadCommand(template, url, vmTemplateHost.getInstallPath(), vmTemplateHost.getSize());
            final UploadListener ul =
                    new UploadListener(secStore, _timer, _uploadDao, uploadTemplateObj, this, ucmd, template.getAccountId(), template.getName(), type, eventId, asyncJobId,
                            asyncMgr);
            _listenerMap.put(uploadTemplateObj, ul);
            try {
                final EndPoint ep = _epSelector.select(secStore);
                if (ep == null) {
                    final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                    s_logger.error(errMsg);
                    return null;
                }
                ep.sendMessageAsync(ucmd, new UploadListener.Callback(ep.getId(), ul));
            } catch (final Exception e) {
                s_logger.warn("Unable to start upload of " + template.getUniqueName() + " from " + secStore.getName() + " to " + url, e);
                ul.setDisconnected();
                ul.scheduleStatusCheck(RequestType.GET_OR_RESTART);
            }
            return uploadTemplateObj.getId();
        }
        return null;
    }

    @Override
    public boolean isTypeUploadInProgress(final Long typeId, final Type type) {
        final List<UploadVO> uploadsInProgress = _uploadDao.listByTypeUploadStatus(typeId, type, UploadVO.Status.UPLOAD_IN_PROGRESS);

        if (uploadsInProgress.size() > 0) {
            return true;
        } else if (type == Type.VOLUME && _uploadDao.listByTypeUploadStatus(typeId, type, UploadVO.Status.COPY_IN_PROGRESS).size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void handleUploadSync(final long sserverId) {

        final HostVO storageHost = _serverDao.findById(sserverId);
        if (storageHost == null) {
            s_logger.warn("Huh? Agent id " + sserverId + " does not correspond to a row in hosts table?");
            return;
        }
        s_logger.debug("Handling upload sserverId " + sserverId);
        final List<UploadVO> uploadsInProgress = new ArrayList<>();
        uploadsInProgress.addAll(_uploadDao.listByHostAndUploadStatus(sserverId, UploadVO.Status.UPLOAD_IN_PROGRESS));
        uploadsInProgress.addAll(_uploadDao.listByHostAndUploadStatus(sserverId, UploadVO.Status.COPY_IN_PROGRESS));
        if (uploadsInProgress.size() > 0) {
            for (final UploadVO uploadJob : uploadsInProgress) {
                uploadJob.setUploadState(UploadVO.Status.UPLOAD_ERROR);
                uploadJob.setErrorString("Could not complete the upload.");
                uploadJob.setLastUpdated(new Date());
                _uploadDao.update(uploadJob.getId(), uploadJob);
            }
        }
    }

    @Override
    public UploadVO createNewUploadEntry(final Long hostId, final Long typeId, final UploadVO.Status uploadState, final Type type, final String uploadUrl, final Upload.Mode mode) {

        final UploadVO uploadObj = new UploadVO(hostId, typeId, new Date(), uploadState, type, uploadUrl, mode);
        _uploadDao.persist(uploadObj);

        return uploadObj;
    }

    @Override
    public void extractVolume(final UploadVO uploadVolumeObj, final DataStore secStore, final VolumeVO volume, final String url, final Long dataCenterId, final String
            installPath, final long eventId,
                              final long asyncJobId, final AsyncJobManager asyncMgr) {

        uploadVolumeObj.setUploadState(Upload.Status.NOT_UPLOADED);
        _uploadDao.update(uploadVolumeObj.getId(), uploadVolumeObj);

        start();
        final UploadCommand ucmd = new UploadCommand(url, volume.getId(), volume.getSize(), installPath, Type.VOLUME);
        final UploadListener ul =
                new UploadListener(secStore, _timer, _uploadDao, uploadVolumeObj, this, ucmd, volume.getAccountId(), volume.getName(), Type.VOLUME, eventId, asyncJobId,
                        asyncMgr);
        _listenerMap.put(uploadVolumeObj, ul);

        try {
            final EndPoint ep = _epSelector.select(secStore);
            if (ep == null) {
                final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                s_logger.error(errMsg);
                return;
            }
            ep.sendMessageAsync(ucmd, new UploadListener.Callback(ep.getId(), ul));
        } catch (final Exception e) {
            s_logger.warn("Unable to start upload of volume " + volume.getName() + " from " + secStore.getName() + " to " + url, e);
            ul.setDisconnected();
            ul.scheduleStatusCheck(RequestType.GET_OR_RESTART);
        }
    }

    @Override
    public UploadVO createEntityDownloadURL(final VMTemplateVO template, final TemplateDataStoreVO vmTemplateHost, final Long dataCenterId, final long eventId) {

        String errorString = "";
        boolean success = false;
        final Type type = (template.getFormat() == ImageFormat.ISO) ? Type.ISO : Type.TEMPLATE;

        // find an endpoint to send command
        final DataStore store = storeMgr.getDataStore(vmTemplateHost.getDataStoreId(), DataStoreRole.Image);
        final EndPoint ep = _epSelector.select(store);
        if (ep == null) {
            final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
            s_logger.error(errMsg);
            return null;
        }

        //Check if it already exists.
        final List<UploadVO> extractURLList = _uploadDao.listByTypeUploadStatus(template.getId(), type, UploadVO.Status.DOWNLOAD_URL_CREATED);
        if (extractURLList.size() > 0) {
            // do some check here
            final UploadVO upload = extractURLList.get(0);
            String uploadUrl = extractURLList.get(0).getUploadUrl();
            final String[] token = uploadUrl.split("/");
            // example: uploadUrl = https://10-11-101-112.realhostip.com/userdata/2fdd9a70-9c4a-4a04-b1d5-1e41c221a1f9.iso
            // then token[2] = 10-11-101-112.realhostip.com, token[4] = 2fdd9a70-9c4a-4a04-b1d5-1e41c221a1f9.iso
            final String hostname = ep.getPublicAddr().replace(".", "-") + ".";
            if ((token != null) && (token.length == 5) && (token[2].equals(hostname + _ssvmUrlDomain))) // ssvm publicip and domain suffix not changed
            {
                return extractURLList.get(0);
            } else if ((token != null) && (token.length == 5) && (token[2].startsWith(hostname))) { // domain suffix changed
                final String uuid = token[4];
                uploadUrl = generateCopyUrl(ep.getPublicAddr(), uuid);
                final UploadVO vo = _uploadDao.createForUpdate();
                vo.setLastUpdated(new Date());
                vo.setUploadUrl(uploadUrl);
                _uploadDao.update(upload.getId(), vo);
                return _uploadDao.findById(upload.getId(), true);
            } else { // ssvm publicip changed
                return null;
            }
        }

        // It doesn't exist so create a DB entry.
        final UploadVO uploadTemplateObj =
                new UploadVO(vmTemplateHost.getDataStoreId(), template.getId(), new Date(), Status.DOWNLOAD_URL_NOT_CREATED, 0, type, Mode.HTTP_DOWNLOAD);
        uploadTemplateObj.setInstallPath(vmTemplateHost.getInstallPath());
        _uploadDao.persist(uploadTemplateObj);

        try {
            // Create Symlink at ssvm
            final String path = vmTemplateHost.getInstallPath();
            final String uuid = UUID.randomUUID().toString() + "." + template.getFormat().getFileExtension(); // adding "." + vhd/ova... etc.
            final CreateEntityDownloadURLCommand cmd = new CreateEntityDownloadURLCommand(((ImageStoreEntity) store).getMountPoint(), path, uuid, null);
            final Answer ans = ep.sendMessage(cmd);
            if (ans == null || !ans.getResult()) {
                errorString = "Unable to create a link for " + type + " id:" + template.getId() + "," + (ans == null ? "" : ans.getDetails());
                s_logger.error(errorString);
                throw new CloudRuntimeException(errorString);
            }

            //Construct actual URL locally now that the symlink exists at SSVM
            final String extractURL = generateCopyUrl(ep.getPublicAddr(), uuid);
            final UploadVO vo = _uploadDao.createForUpdate();
            vo.setLastUpdated(new Date());
            vo.setUploadUrl(extractURL);
            vo.setUploadState(Status.DOWNLOAD_URL_CREATED);
            _uploadDao.update(uploadTemplateObj.getId(), vo);
            success = true;
            return _uploadDao.findById(uploadTemplateObj.getId(), true);
        } finally {
            if (!success) {
                final UploadVO uploadJob = _uploadDao.createForUpdate(uploadTemplateObj.getId());
                uploadJob.setLastUpdated(new Date());
                uploadJob.setErrorString(errorString);
                uploadJob.setUploadState(Status.ERROR);
                _uploadDao.update(uploadTemplateObj.getId(), uploadJob);
            }
        }
    }

    @Override
    public void createVolumeDownloadURL(final Long entityId, final String path, final Type type, final Long dataCenterId, final Long uploadId, final ImageFormat format) {

        String errorString = "";
        boolean success = false;
        try {
            final List<HostVO> storageServers = _resourceMgr.listAllHostsInOneZoneByType(Host.Type.SecondaryStorage, dataCenterId);
            if (storageServers == null) {
                errorString = "No Storage Server found at the datacenter - " + dataCenterId;
                throw new CloudRuntimeException(errorString);
            }

            // Update DB for state = DOWNLOAD_URL_NOT_CREATED.
            final UploadVO uploadJob = _uploadDao.createForUpdate(uploadId);
            uploadJob.setUploadState(Status.DOWNLOAD_URL_NOT_CREATED);
            uploadJob.setLastUpdated(new Date());
            _uploadDao.update(uploadJob.getId(), uploadJob);

            // Create Symlink at ssvm
            final String uuid = UUID.randomUUID().toString() + "." + format.toString().toLowerCase();
            final DataStore secStore = storeMgr.getDataStore(ApiDBUtils.findUploadById(uploadId).getDataStoreId(), DataStoreRole.Image);
            final EndPoint ep = _epSelector.select(secStore);
            if (ep == null) {
                errorString = "There is no secondary storage VM for secondary storage host " + secStore.getName();
                throw new CloudRuntimeException(errorString);
            }

            final CreateEntityDownloadURLCommand cmd = new CreateEntityDownloadURLCommand(((ImageStoreEntity) secStore).getMountPoint(), path, uuid, null);
            final Answer ans = ep.sendMessage(cmd);
            if (ans == null || !ans.getResult()) {
                errorString = "Unable to create a link for " + type + " id:" + entityId + "," + (ans == null ? "" : ans.getDetails());
                s_logger.warn(errorString);
                throw new CloudRuntimeException(errorString);
            }

            final List<SecondaryStorageVmVO> ssVms = _secStorageVmDao.getSecStorageVmListInStates(SecondaryStorageVm.Role.templateProcessor, dataCenterId, State.Running);
            if (ssVms.size() > 0) {
                final SecondaryStorageVmVO ssVm = ssVms.get(0);
                if (ssVm.getPublicIpAddress() == null) {
                    errorString = "A running secondary storage vm has a null public ip?";
                    s_logger.error(errorString);
                    throw new CloudRuntimeException(errorString);
                }
                //Construct actual URL locally now that the symlink exists at SSVM
                final String extractURL = generateCopyUrl(ssVm.getPublicIpAddress(), uuid);
                final UploadVO vo = _uploadDao.createForUpdate();
                vo.setLastUpdated(new Date());
                vo.setUploadUrl(extractURL);
                vo.setUploadState(Status.DOWNLOAD_URL_CREATED);
                _uploadDao.update(uploadId, vo);
                success = true;
                return;
            }
            errorString = "Couldnt find a running SSVM in the zone" + dataCenterId + ". Couldnt create the extraction URL.";
            throw new CloudRuntimeException(errorString);
        } finally {
            if (!success) {
                final UploadVO uploadJob = _uploadDao.createForUpdate(uploadId);
                uploadJob.setLastUpdated(new Date());
                uploadJob.setErrorString(errorString);
                uploadJob.setUploadState(Status.ERROR);
                _uploadDao.update(uploadId, uploadJob);
            }
        }
    }

    private String generateCopyUrl(final String ipAddress, final String uuid) {
        String hostname = ipAddress;
        String scheme = "http";
        if (_sslCopy) {
            hostname = ipAddress.replace(".", "-");
            scheme = "https";

            // Code for putting in custom certificates.
            if (_ssvmUrlDomain != null && _ssvmUrlDomain.length() > 0) {
                hostname = hostname + "." + _ssvmUrlDomain;
            } else {
                hostname = hostname + ".realhostip.com";
            }
        }
        return scheme + "://" + hostname + "/userdata/" + uuid;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        final Map<String, String> configs = _configDao.getConfiguration("management-server", params);
        _sslCopy = Boolean.parseBoolean(configs.get("secstorage.encrypt.copy"));

        final String cert = configs.get("secstorage.secure.copy.cert");
        if ("realhostip.com".equalsIgnoreCase(cert)) {
            s_logger.warn("Only realhostip.com ssl cert is supported, ignoring self-signed and other certs");
        }

        _ssvmUrlDomain = configs.get("secstorage.ssl.cert.domain");

        _agentMgr.registerForHostEvents(new UploadListener(this), true, false, false);
        final String cleanupInterval = configs.get("extract.url.cleanup.interval");
        _cleanupInterval = NumbersUtil.parseInt(cleanupInterval, 7200);

        final String urlExpirationInterval = configs.get("extract.url.expiration.interval");
        _urlExpirationInterval = NumbersUtil.parseInt(urlExpirationInterval, 14400);

        final String workers = (String) params.get("expunge.workers");
        final int wrks = NumbersUtil.parseInt(workers, 1);
        _executor = Executors.newScheduledThreadPool(wrks, new NamedThreadFactory("UploadMonitor-Scavenger"));
        return true;
    }

    @Override
    public boolean start() {
        _executor.scheduleWithFixedDelay(new StorageGarbageCollector(), _cleanupInterval, _cleanupInterval, TimeUnit.SECONDS);
        _timer = new Timer();
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    public void handleUploadEvent(final Long accountId, final String typeName, final Type type, final Long uploadId, final com.cloud.storage.Upload.Status reason, final long
            eventId) {

        if ((reason == Upload.Status.UPLOADED) || (reason == Upload.Status.ABANDONED)) {
            final UploadVO uploadObj = new UploadVO(uploadId);
            final UploadListener oldListener = _listenerMap.get(uploadObj);
            if (oldListener != null) {
                _listenerMap.remove(uploadObj);
            }
        }
    }

    public void cleanupStorage() {

        final int EXTRACT_URL_LIFE_LIMIT_IN_SECONDS = _urlExpirationInterval;
        final List<UploadVO> extractJobs = _uploadDao.listByModeAndStatus(Mode.HTTP_DOWNLOAD, Status.DOWNLOAD_URL_CREATED);

        for (final UploadVO extractJob : extractJobs) {
            if (getTimeDiff(extractJob.getLastUpdated()) > EXTRACT_URL_LIFE_LIMIT_IN_SECONDS) {
                final String path = extractJob.getInstallPath();
                final DataStore secStore = storeMgr.getDataStore(extractJob.getDataStoreId(), DataStoreRole.Image);

                // Would delete the symlink for the Type and if Type == VOLUME then also the volume
                final DeleteEntityDownloadURLCommand cmd =
                        new DeleteEntityDownloadURLCommand(path, extractJob.getType(), extractJob.getUploadUrl(), ((ImageStoreVO) secStore).getParent());
                final EndPoint ep = _epSelector.select(secStore);
                if (ep == null) {
                    s_logger.warn("UploadMonitor cleanup: There is no secondary storage VM for secondary storage host " + extractJob.getDataStoreId());
                    continue; //TODO: why continue? why not break?
                }
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("UploadMonitor cleanup: Sending deletion of extract URL " + extractJob.getUploadUrl() + " to ssvm " + ep.getHostAddr());
                }
                final Answer ans = ep.sendMessage(cmd);
                if (ans != null && ans.getResult()) {
                    _uploadDao.remove(extractJob.getId());
                } else {
                    s_logger.warn("UploadMonitor cleanup: Unable to delete the link for " + extractJob.getType() + " id=" + extractJob.getTypeId() + " url=" +
                            extractJob.getUploadUrl() + " on ssvm " + ep.getHostAddr());
                }
            }
        }
    }

    private long getTimeDiff(final Date date) {
        final Calendar currentDateCalendar = Calendar.getInstance();
        final Calendar givenDateCalendar = Calendar.getInstance();
        givenDateCalendar.setTime(date);

        return (currentDateCalendar.getTimeInMillis() - givenDateCalendar.getTimeInMillis()) / 1000;
    }

    protected class StorageGarbageCollector extends ManagedContextRunnable {

        public StorageGarbageCollector() {
        }

        @Override
        protected void runInContext() {
            try {
                final GlobalLock scanLock = GlobalLock.getInternLock("uploadmonitor.storageGC");
                try {
                    if (scanLock.lock(3)) {
                        try {
                            cleanupStorage();
                        } finally {
                            scanLock.unlock();
                        }
                    }
                } finally {
                    scanLock.releaseRef();
                }
            } catch (final Exception e) {
                s_logger.error("Caught the following Exception", e);
            }
        }
    }
}
