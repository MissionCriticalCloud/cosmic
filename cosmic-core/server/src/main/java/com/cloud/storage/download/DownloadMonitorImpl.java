package com.cloud.storage.download;

import com.cloud.agent.AgentManager;
import com.cloud.common.storageprocessor.TemplateConstants;
import com.cloud.configuration.Config;
import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.EndPoint;
import com.cloud.engine.subsystem.api.storage.EndPointSelector;
import com.cloud.engine.subsystem.api.storage.VolumeInfo;
import com.cloud.framework.async.AsyncCompletionCallback;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.legacymodel.communication.answer.DownloadAnswer;
import com.cloud.legacymodel.communication.command.DownloadCommand;
import com.cloud.legacymodel.communication.command.DownloadCommand.ResourceType;
import com.cloud.legacymodel.communication.command.DownloadProgressCommand;
import com.cloud.legacymodel.communication.command.DownloadProgressCommand.RequestType;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.network.Proxy;
import com.cloud.legacymodel.storage.VMTemplateStatus;
import com.cloud.legacymodel.storage.VirtualMachineTemplate;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.to.TemplateObjectTO;
import com.cloud.legacymodel.to.VolumeObjectTO;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.storage.RegisterVolumePayload;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.TemplateDataStoreDao;
import com.cloud.storage.datastore.db.TemplateDataStoreVO;
import com.cloud.storage.datastore.db.VolumeDataStoreDao;
import com.cloud.storage.datastore.db.VolumeDataStoreVO;
import com.cloud.storage.upload.UploadListener;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.component.ManagerBase;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DownloadMonitorImpl extends ManagerBase implements DownloadMonitor {
    static final Logger s_logger = LoggerFactory.getLogger(DownloadMonitorImpl.class);
    @Inject
    private final VMTemplateDao _templateDao = null;
    @Inject
    private TemplateDataStoreDao _vmTemplateStoreDao;
    @Inject
    private VolumeDao _volumeDao;
    @Inject
    private VolumeDataStoreDao _volumeStoreDao;
    @Inject
    private AgentManager _agentMgr;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private EndPointSelector _epSelector;

    private String _copyAuthPasswd;
    private String _proxy = null;

    private Timer _timer;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) {
        final Map<String, String> configs = this._configDao.getConfiguration("management-server", params);
        this._proxy = configs.get(Config.SecStorageProxy.key());

        final String cert = configs.get("secstorage.ssl.cert.domain");
        if (!"realhostip.com".equalsIgnoreCase(cert)) {
            s_logger.warn("Only realhostip.com ssl cert is supported, ignoring self-signed and other certs");
        }

        this._copyAuthPasswd = configs.get("secstorage.copy.password");

        final DownloadListener dl = new DownloadListener(this);
        ComponentContext.inject(dl);
        this._agentMgr.registerForHostEvents(dl, true, false, false);

        return true;
    }

    @Override
    public boolean start() {
        this._timer = new Timer();
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public void downloadTemplateToStorage(final DataObject template, final AsyncCompletionCallback<DownloadAnswer> callback) {
        if (template != null) {
            final long templateId = template.getId();
            final DataStore store = template.getDataStore();
            if (isTemplateUpdateable(templateId, store.getId())) {
                if (template.getUri() != null) {
                    initiateTemplateDownload(template, callback);
                } else {
                    s_logger.info("Template url is null, cannot download");
                    final DownloadAnswer ans = new DownloadAnswer("Template url is null", VMTemplateStatus.UNKNOWN);
                    callback.complete(ans);
                }
            } else {
                s_logger.info("Template download is already in progress or already downloaded");
                final DownloadAnswer ans =
                        new DownloadAnswer("Template download is already in progress or already downloaded", VMTemplateStatus.UNKNOWN);
                callback.complete(ans);
            }
        }
    }

    public boolean isTemplateUpdateable(final Long templateId, final Long storeId) {
        final List<TemplateDataStoreVO> downloadsInProgress =
                this._vmTemplateStoreDao.listByTemplateStoreDownloadStatus(templateId, storeId, VMTemplateStatus.DOWNLOAD_IN_PROGRESS, VMTemplateStatus.DOWNLOADED);
        return (downloadsInProgress.size() == 0);
    }

    private void initiateTemplateDownload(final DataObject template, final AsyncCompletionCallback<DownloadAnswer> callback) {
        boolean downloadJobExists = false;
        TemplateDataStoreVO vmTemplateStore = null;
        final DataStore store = template.getDataStore();

        vmTemplateStore = this._vmTemplateStoreDao.findByStoreTemplate(store.getId(), template.getId());
        if (vmTemplateStore == null) {
            vmTemplateStore =
                    new TemplateDataStoreVO(store.getId(), template.getId(), new Date(), 0, VMTemplateStatus.NOT_DOWNLOADED, null, null, "jobid0000", null, template.getUri());
            vmTemplateStore.setDataStoreRole(store.getRole());
            vmTemplateStore = this._vmTemplateStoreDao.persist(vmTemplateStore);
        } else if ((vmTemplateStore.getJobId() != null) && (vmTemplateStore.getJobId().length() > 2)) {
            downloadJobExists = true;
        }

        final Long maxTemplateSizeInBytes = getMaxTemplateSizeInBytes();
        if (vmTemplateStore != null) {
            start();
            final VirtualMachineTemplate tmpl = this._templateDao.findById(template.getId());
            DownloadCommand dcmd = new DownloadCommand((TemplateObjectTO) (template.getTO()), maxTemplateSizeInBytes);
            dcmd.setProxy(getHttpProxy());
            if (downloadJobExists) {
                dcmd = new DownloadProgressCommand(dcmd, vmTemplateStore.getJobId(), RequestType.GET_OR_RESTART);
            }
            if (vmTemplateStore.isCopy()) {
                dcmd.setCreds(TemplateConstants.DEFAULT_HTTP_AUTH_USER, this._copyAuthPasswd);
            }
            final EndPoint ep = this._epSelector.select(template);
            if (ep == null) {
                final String errMsg = "There is no secondary storage VM for downloading template to image store " + store.getName();
                s_logger.warn(errMsg);
                throw new CloudRuntimeException(errMsg);
            }
            final DownloadListener dl = new DownloadListener(ep, store, template, this._timer, this, dcmd, callback);
            ComponentContext.inject(dl);  // initialize those auto-wired field in download listener.
            if (downloadJobExists) {
                // due to handling existing download job issues, we still keep
                // downloadState in template_store_ref to avoid big change in
                // DownloadListener to use
                // new ObjectInDataStore.State transition. TODO: fix this later
                // to be able to remove downloadState from template_store_ref.
                s_logger.info("found existing download job");
                dl.setCurrState(vmTemplateStore.getDownloadState());
            }

            try {
                ep.sendMessageAsync(dcmd, new UploadListener.Callback(ep.getId(), dl));
            } catch (final Exception e) {
                s_logger.warn("Unable to start /resume download of template " + template.getId() + " to " + store.getName(), e);
                dl.setDisconnected();
                dl.scheduleStatusCheck(RequestType.GET_OR_RESTART);
            }
        }
    }

    private Long getMaxTemplateSizeInBytes() {
        try {
            return Long.parseLong(this._configDao.getValue("max.template.iso.size")) * 1024L * 1024L * 1024L;
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    private Proxy getHttpProxy() {
        if (this._proxy == null) {
            return null;
        }
        try {
            final URI uri = new URI(this._proxy);
            final Proxy prx = new Proxy(uri);
            return prx;
        } catch (final URISyntaxException e) {
            return null;
        }
    }

    @Override
    public void downloadVolumeToStorage(final DataObject volume, final AsyncCompletionCallback<DownloadAnswer> callback) {
        boolean downloadJobExists = false;
        VolumeDataStoreVO volumeHost = null;
        final DataStore store = volume.getDataStore();
        final VolumeInfo volInfo = (VolumeInfo) volume;
        final RegisterVolumePayload payload = (RegisterVolumePayload) volInfo.getpayload();
        final String url = payload.getUrl();
        final String checkSum = payload.getChecksum();
        final ImageFormat format = ImageFormat.valueOf(payload.getFormat());

        volumeHost = this._volumeStoreDao.findByStoreVolume(store.getId(), volume.getId());
        if (volumeHost == null) {
            volumeHost = new VolumeDataStoreVO(store.getId(), volume.getId(), new Date(), 0, VMTemplateStatus.NOT_DOWNLOADED, null, null, "jobid0000", null, url, checkSum);
            this._volumeStoreDao.persist(volumeHost);
        } else if ((volumeHost.getJobId() != null) && (volumeHost.getJobId().length() > 2)) {
            downloadJobExists = true;
        } else {
            // persit url and checksum
            volumeHost.setDownloadUrl(url);
            volumeHost.setChecksum(checkSum);
            this._volumeStoreDao.update(volumeHost.getId(), volumeHost);
        }

        final Long maxVolumeSizeInBytes = getMaxVolumeSizeInBytes();
        if (volumeHost != null) {
            start();
            final Volume vol = this._volumeDao.findById(volume.getId());
            DownloadCommand dcmd = new DownloadCommand((VolumeObjectTO) (volume.getTO()), maxVolumeSizeInBytes, checkSum, url, format);
            dcmd.setProxy(getHttpProxy());
            if (downloadJobExists) {
                dcmd = new DownloadProgressCommand(dcmd, volumeHost.getJobId(), RequestType.GET_OR_RESTART);
                dcmd.setResourceType(ResourceType.VOLUME);
            }

            final EndPoint ep = this._epSelector.select(volume);
            if (ep == null) {
                s_logger.warn("There is no secondary storage VM for image store " + store.getName());
                return;
            }
            final DownloadListener dl = new DownloadListener(ep, store, volume, this._timer, this, dcmd, callback);
            ComponentContext.inject(dl); // auto-wired those injected fields in DownloadListener

            if (downloadJobExists) {
                dl.setCurrState(volumeHost.getDownloadState());
            }

            try {
                ep.sendMessageAsync(dcmd, new UploadListener.Callback(ep.getId(), dl));
            } catch (final Exception e) {
                s_logger.warn("Unable to start /resume download of volume " + volume.getId() + " to " + store.getName(), e);
                dl.setDisconnected();
                dl.scheduleStatusCheck(RequestType.GET_OR_RESTART);
            }
        }
    }

    private Long getMaxVolumeSizeInBytes() {
        try {
            return Long.parseLong(this._configDao.getValue("storage.max.volume.upload.size")) * 1024L * 1024L * 1024L;
        } catch (final NumberFormatException e) {
            return null;
        }
    }
}
