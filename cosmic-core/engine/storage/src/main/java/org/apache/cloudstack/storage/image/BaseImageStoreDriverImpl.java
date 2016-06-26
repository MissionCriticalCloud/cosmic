package org.apache.cloudstack.storage.image;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.DownloadAnswer;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataTO;
import com.cloud.alert.AlertManager;
import com.cloud.storage.Upload;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateZoneDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.download.DownloadMonitor;
import com.cloud.utils.net.Proxy;
import org.apache.cloudstack.engine.subsystem.api.storage.CopyCommandResult;
import org.apache.cloudstack.engine.subsystem.api.storage.CreateCmdResult;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.framework.async.AsyncCallbackDispatcher;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.framework.async.AsyncRpcContext;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.storage.command.CommandResult;
import org.apache.cloudstack.storage.command.DeleteCommand;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.TemplateDataStoreVO;
import org.apache.cloudstack.storage.datastore.db.VolumeDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.VolumeDataStoreVO;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseImageStoreDriverImpl implements ImageStoreDriver {
    private static final Logger s_logger = LoggerFactory.getLogger(BaseImageStoreDriverImpl.class);
    @Inject
    protected VMTemplateDao _templateDao;
    protected String _proxy = null;
    @Inject
    DownloadMonitor _downloadMonitor;
    @Inject
    VolumeDao volumeDao;
    @Inject
    VolumeDataStoreDao _volumeStoreDao;
    @Inject
    TemplateDataStoreDao _templateStoreDao;
    @Inject
    EndPointSelector _epSelector;
    @Inject
    ConfigurationDao configDao;
    @Inject
    VMTemplateZoneDao _vmTemplateZoneDao;
    @Inject
    AlertManager _alertMgr;

    protected Proxy getHttpProxy() {
        if (_proxy == null) {
            return null;
        }
        try {
            final URI uri = new URI(_proxy);
            final Proxy prx = new Proxy(uri);
            return prx;
        } catch (final URISyntaxException e) {
            return null;
        }
    }

    @Override
    public Map<String, String> getCapabilities() {
        return null;
    }

    @Override
    public DataTO getTO(final DataObject data) {
        return null;
    }

    @Override
    public void createAsync(final DataStore dataStore, final DataObject data, final AsyncCompletionCallback<CreateCmdResult> callback) {
        final CreateContext<CreateCmdResult> context = new CreateContext<>(callback, data);
        final AsyncCallbackDispatcher<BaseImageStoreDriverImpl, DownloadAnswer> caller = AsyncCallbackDispatcher.create(this);
        caller.setContext(context);
        if (data.getType() == DataObjectType.TEMPLATE) {
            caller.setCallback(caller.getTarget().createTemplateAsyncCallback(null, null));
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Downloading template to data store " + dataStore.getId());
            }
            _downloadMonitor.downloadTemplateToStorage(data, caller);
        } else if (data.getType() == DataObjectType.VOLUME) {
            caller.setCallback(caller.getTarget().createVolumeAsyncCallback(null, null));
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Downloading volume to data store " + dataStore.getId());
            }
            _downloadMonitor.downloadVolumeToStorage(data, caller);
        }
    }

    protected Void createTemplateAsyncCallback(final AsyncCallbackDispatcher<? extends BaseImageStoreDriverImpl, DownloadAnswer> callback,
                                               final CreateContext<CreateCmdResult> context) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Performing image store createTemplate async callback");
        }
        final DownloadAnswer answer = callback.getResult();
        final DataObject obj = context.data;
        final DataStore store = obj.getDataStore();

        final TemplateDataStoreVO tmpltStoreVO = _templateStoreDao.findByStoreTemplate(store.getId(), obj.getId());
        if (tmpltStoreVO != null) {
            if (tmpltStoreVO.getDownloadState() == VMTemplateStorageResourceAssoc.Status.DOWNLOADED) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Template is already in DOWNLOADED state, ignore further incoming DownloadAnswer");
                }
                return null;
            }
            final TemplateDataStoreVO updateBuilder = _templateStoreDao.createForUpdate();
            updateBuilder.setDownloadPercent(answer.getDownloadPct());
            updateBuilder.setDownloadState(answer.getDownloadStatus());
            updateBuilder.setLastUpdated(new Date());
            updateBuilder.setErrorString(answer.getErrorString());
            updateBuilder.setJobId(answer.getJobId());
            updateBuilder.setLocalDownloadPath(answer.getDownloadPath());
            updateBuilder.setInstallPath(answer.getInstallPath());
            updateBuilder.setSize(answer.getTemplateSize());
            updateBuilder.setPhysicalSize(answer.getTemplatePhySicalSize());
            _templateStoreDao.update(tmpltStoreVO.getId(), updateBuilder);
            // update size in vm_template table
            final VMTemplateVO tmlptUpdater = _templateDao.createForUpdate();
            tmlptUpdater.setSize(answer.getTemplateSize());
            _templateDao.update(obj.getId(), tmlptUpdater);
        }

        final AsyncCompletionCallback<CreateCmdResult> caller = context.getParentCallback();

        if (answer.getDownloadStatus() == VMTemplateStorageResourceAssoc.Status.DOWNLOAD_ERROR ||
                answer.getDownloadStatus() == VMTemplateStorageResourceAssoc.Status.ABANDONED || answer.getDownloadStatus() == VMTemplateStorageResourceAssoc.Status.UNKNOWN) {
            final CreateCmdResult result = new CreateCmdResult(null, null);
            result.setSuccess(false);
            result.setResult(answer.getErrorString());
            caller.complete(result);
            final String msg = "Failed to register template: " + obj.getUuid() + " with error: " + answer.getErrorString();
            _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_UPLOAD_FAILED, _vmTemplateZoneDao.listByTemplateId(obj.getId()).get(0).getZoneId(), null, msg, msg);
            s_logger.error(msg);
        } else if (answer.getDownloadStatus() == VMTemplateStorageResourceAssoc.Status.DOWNLOADED) {
            if (answer.getCheckSum() != null) {
                final VMTemplateVO templateDaoBuilder = _templateDao.createForUpdate();
                templateDaoBuilder.setChecksum(answer.getCheckSum());
                _templateDao.update(obj.getId(), templateDaoBuilder);
            }

            final CreateCmdResult result = new CreateCmdResult(null, null);
            caller.complete(result);
        }
        return null;
    }

    protected Void
    createVolumeAsyncCallback(final AsyncCallbackDispatcher<? extends BaseImageStoreDriverImpl, DownloadAnswer> callback, final CreateContext<CreateCmdResult> context) {
        final DownloadAnswer answer = callback.getResult();
        final DataObject obj = context.data;
        final DataStore store = obj.getDataStore();

        final VolumeDataStoreVO volStoreVO = _volumeStoreDao.findByStoreVolume(store.getId(), obj.getId());
        if (volStoreVO != null) {
            if (volStoreVO.getDownloadState() == VMTemplateStorageResourceAssoc.Status.DOWNLOADED) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Volume is already in DOWNLOADED state, ignore further incoming DownloadAnswer");
                }
                return null;
            }
            final VolumeDataStoreVO updateBuilder = _volumeStoreDao.createForUpdate();
            updateBuilder.setDownloadPercent(answer.getDownloadPct());
            updateBuilder.setDownloadState(answer.getDownloadStatus());
            updateBuilder.setLastUpdated(new Date());
            updateBuilder.setErrorString(answer.getErrorString());
            updateBuilder.setJobId(answer.getJobId());
            updateBuilder.setLocalDownloadPath(answer.getDownloadPath());
            updateBuilder.setInstallPath(answer.getInstallPath());
            updateBuilder.setSize(answer.getTemplateSize());
            updateBuilder.setPhysicalSize(answer.getTemplatePhySicalSize());
            _volumeStoreDao.update(volStoreVO.getId(), updateBuilder);
            // update size in volume table
            final VolumeVO volUpdater = volumeDao.createForUpdate();
            volUpdater.setSize(answer.getTemplateSize());
            volumeDao.update(obj.getId(), volUpdater);
        }

        final AsyncCompletionCallback<CreateCmdResult> caller = context.getParentCallback();

        if (answer.getDownloadStatus() == VMTemplateStorageResourceAssoc.Status.DOWNLOAD_ERROR ||
                answer.getDownloadStatus() == VMTemplateStorageResourceAssoc.Status.ABANDONED || answer.getDownloadStatus() == VMTemplateStorageResourceAssoc.Status.UNKNOWN) {
            final CreateCmdResult result = new CreateCmdResult(null, null);
            result.setSuccess(false);
            result.setResult(answer.getErrorString());
            caller.complete(result);
            final String msg = "Failed to upload volume: " + obj.getUuid() + " with error: " + answer.getErrorString();
            _alertMgr.sendAlert(AlertManager.AlertType.ALERT_TYPE_UPLOAD_FAILED,
                    (volStoreVO == null ? -1L : volStoreVO.getZoneId()), null, msg, msg);
            s_logger.error(msg);
        } else if (answer.getDownloadStatus() == VMTemplateStorageResourceAssoc.Status.DOWNLOADED) {
            final CreateCmdResult result = new CreateCmdResult(null, null);
            caller.complete(result);
        }
        return null;
    }

    @Override
    public void deleteAsync(final DataStore dataStore, final DataObject data, final AsyncCompletionCallback<CommandResult> callback) {
        final CommandResult result = new CommandResult();
        try {
            final DeleteCommand cmd = new DeleteCommand(data.getTO());
            final EndPoint ep = _epSelector.select(data);
            Answer answer = null;
            if (ep == null) {
                final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                s_logger.error(errMsg);
                answer = new Answer(cmd, false, errMsg);
            } else {
                answer = ep.sendMessage(cmd);
            }
            if (answer != null && !answer.getResult()) {
                result.setResult(answer.getDetails());
            }
        } catch (final Exception ex) {
            s_logger.debug("Unable to destoy " + data.getType().toString() + ": " + data.getId(), ex);
            result.setResult(ex.toString());
        }
        callback.complete(result);
    }

    @Override
    public void copyAsync(final DataObject srcdata, final DataObject destData, final AsyncCompletionCallback<CopyCommandResult> callback) {
    }

    @Override
    public boolean canCopy(final DataObject srcData, final DataObject destData) {
        return false;
    }

    @Override
    public void resize(final DataObject data, final AsyncCompletionCallback<CreateCmdResult> callback) {
    }

    protected Long getMaxTemplateSizeInBytes() {
        try {
            return Long.parseLong(configDao.getValue("max.template.iso.size")) * 1024L * 1024L * 1024L;
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void deleteEntityExtractUrl(final DataStore store, final String installPath, final String url, final Upload.Type entityType) {
    }

    protected class CreateContext<T> extends AsyncRpcContext<T> {
        final DataObject data;

        public CreateContext(final AsyncCompletionCallback<T> callback, final DataObject data) {
            super(callback);
            this.data = data;
        }
    }
}
