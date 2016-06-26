package org.apache.cloudstack.storage.datastore.driver;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.ResizeVolumeAnswer;
import com.cloud.agent.api.storage.ResizeVolumeCommand;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.configuration.Config;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.host.Host;
import com.cloud.host.dao.HostDao;
import com.cloud.storage.CreateSnapshotPayload;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.ResizeVolumePayload;
import com.cloud.storage.Storage;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePool;
import com.cloud.storage.Volume;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.snapshot.SnapshotManager;
import com.cloud.template.TemplateManager;
import com.cloud.utils.NumbersUtil;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.engine.subsystem.api.storage.ChapInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.CopyCommandResult;
import org.apache.cloudstack.engine.subsystem.api.storage.CreateCmdResult;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreCapabilities;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreDriver;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.StorageAction;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.storage.command.CommandResult;
import org.apache.cloudstack.storage.command.CopyCmdAnswer;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.command.CreateObjectCommand;
import org.apache.cloudstack.storage.command.DeleteCommand;
import org.apache.cloudstack.storage.command.RevertSnapshotCommand;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.apache.cloudstack.storage.to.SnapshotObjectTO;
import org.apache.cloudstack.storage.to.TemplateObjectTO;
import org.apache.cloudstack.storage.volume.VolumeObject;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudStackPrimaryDataStoreDriverImpl implements PrimaryDataStoreDriver {
    private static final Logger s_logger = LoggerFactory.getLogger(CloudStackPrimaryDataStoreDriverImpl.class);
    @Inject
    DiskOfferingDao diskOfferingDao;
    @Inject
    VMTemplateDao templateDao;
    @Inject
    VolumeDao volumeDao;
    @Inject
    HostDao hostDao;
    @Inject
    StorageManager storageMgr;
    @Inject
    VMInstanceDao vmDao;
    @Inject
    SnapshotDao snapshotDao;
    @Inject
    PrimaryDataStoreDao primaryStoreDao;
    @Inject
    SnapshotManager snapshotMgr;
    @Inject
    EndPointSelector epSelector;
    @Inject
    ConfigurationDao configDao;
    @Inject
    TemplateManager templateManager;
    @Inject
    TemplateDataFactory templateDataFactory;

    @Override
    public Map<String, String> getCapabilities() {
        final Map<String, String> caps = new HashMap<>();
        caps.put(DataStoreCapabilities.VOLUME_SNAPSHOT_QUIESCEVM.toString(), "false");
        return caps;
    }

    @Override
    public DataTO getTO(final DataObject data) {
        return null;
    }

    @Override
    public DataStoreTO getStoreTO(final DataStore store) {
        return null;
    }

    @Override
    public void createAsync(final DataStore dataStore, final DataObject data, final AsyncCompletionCallback<CreateCmdResult> callback) {
        String errMsg = null;
        Answer answer = null;
        final CreateCmdResult result = new CreateCmdResult(null, null);
        if (data.getType() == DataObjectType.VOLUME) {
            try {
                answer = createVolume((VolumeInfo) data);
                if ((answer == null) || (!answer.getResult())) {
                    result.setSuccess(false);
                    if (answer != null) {
                        result.setResult(answer.getDetails());
                    }
                } else {
                    result.setAnswer(answer);
                }
            } catch (final StorageUnavailableException e) {
                s_logger.debug("failed to create volume", e);
                errMsg = e.toString();
            } catch (final Exception e) {
                s_logger.debug("failed to create volume", e);
                errMsg = e.toString();
            }
        }
        if (errMsg != null) {
            result.setResult(errMsg);
        }

        callback.complete(result);
    }

    public Answer createVolume(final VolumeInfo volume) throws StorageUnavailableException {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Creating volume: " + volume);
        }

        final CreateObjectCommand cmd = new CreateObjectCommand(volume.getTO());
        final EndPoint ep = epSelector.select(volume);
        Answer answer = null;
        if (ep == null) {
            final String errMsg = "No remote endpoint to send DeleteCommand, check if host or ssvm is down?";
            s_logger.error(errMsg);
            answer = new Answer(cmd, false, errMsg);
        } else {
            answer = ep.sendMessage(cmd);
        }
        return answer;
    }

    @Override
    public void deleteAsync(final DataStore dataStore, final DataObject data, final AsyncCompletionCallback<CommandResult> callback) {
        final DeleteCommand cmd = new DeleteCommand(data.getTO());

        final CommandResult result = new CommandResult();
        try {
            EndPoint ep = null;
            if (data.getType() == DataObjectType.VOLUME) {
                ep = epSelector.select(data, StorageAction.DELETEVOLUME);
            } else {
                ep = epSelector.select(data);
            }
            if (ep == null) {
                final String errMsg = "No remote endpoint to send DeleteCommand, check if host or ssvm is down?";
                s_logger.error(errMsg);
                result.setResult(errMsg);
            } else {
                final Answer answer = ep.sendMessage(cmd);
                if (answer != null && !answer.getResult()) {
                    result.setResult(answer.getDetails());
                }
            }
        } catch (final Exception ex) {
            s_logger.debug("Unable to destoy volume" + data.getId(), ex);
            result.setResult(ex.toString());
        }
        callback.complete(result);
    }

    @Override
    public void copyAsync(final DataObject srcdata, final DataObject destData, final AsyncCompletionCallback<CopyCommandResult> callback) {
        final DataStore store = destData.getDataStore();
        if (store.getRole() == DataStoreRole.Primary) {
            if ((srcdata.getType() == DataObjectType.TEMPLATE && destData.getType() == DataObjectType.TEMPLATE)) {
                //For CLVM, we need to copy template to primary storage at all, just fake the copy result.
                final TemplateObjectTO templateObjectTO = new TemplateObjectTO();
                templateObjectTO.setPath(UUID.randomUUID().toString());
                templateObjectTO.setSize(srcdata.getSize());
                templateObjectTO.setPhysicalSize(srcdata.getSize());
                templateObjectTO.setFormat(Storage.ImageFormat.RAW);
                final CopyCmdAnswer answer = new CopyCmdAnswer(templateObjectTO);
                final CopyCommandResult result = new CopyCommandResult("", answer);
                callback.complete(result);
            } else if (srcdata.getType() == DataObjectType.TEMPLATE && destData.getType() == DataObjectType.VOLUME) {
                //For CLVM, we need to pass template on secondary storage to hypervisor
                final String value = configDao.getValue(Config.PrimaryStorageDownloadWait.toString());
                final int _primaryStorageDownloadWait = NumbersUtil.parseInt(value, Integer.parseInt(Config.PrimaryStorageDownloadWait.getDefaultValue()));
                final StoragePoolVO storagePoolVO = primaryStoreDao.findById(store.getId());
                final DataStore imageStore = templateManager.getImageStore(storagePoolVO.getDataCenterId(), srcdata.getId());
                final DataObject srcData = templateDataFactory.getTemplate(srcdata.getId(), imageStore);

                final CopyCommand cmd = new CopyCommand(srcData.getTO(), destData.getTO(), _primaryStorageDownloadWait, true);
                final EndPoint ep = epSelector.select(srcData, destData);
                Answer answer = null;
                if (ep == null) {
                    final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                    s_logger.error(errMsg);
                    answer = new Answer(cmd, false, errMsg);
                } else {
                    answer = ep.sendMessage(cmd);
                }
                final CopyCommandResult result = new CopyCommandResult("", answer);
                callback.complete(result);
            }
        }
    }

    @Override
    public boolean canCopy(final DataObject srcData, final DataObject destData) {
        //BUG fix for CLOUDSTACK-4618
        final DataStore store = destData.getDataStore();
        if (store.getRole() == DataStoreRole.Primary && srcData.getType() == DataObjectType.TEMPLATE
                && (destData.getType() == DataObjectType.TEMPLATE || destData.getType() == DataObjectType.VOLUME)) {
            final StoragePoolVO storagePoolVO = primaryStoreDao.findById(store.getId());
            if (storagePoolVO != null && storagePoolVO.getPoolType() == Storage.StoragePoolType.CLVM) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void resize(final DataObject data, final AsyncCompletionCallback<CreateCmdResult> callback) {
        final VolumeObject vol = (VolumeObject) data;
        final StoragePool pool = (StoragePool) data.getDataStore();
        final ResizeVolumePayload resizeParameter = (ResizeVolumePayload) vol.getpayload();

        final ResizeVolumeCommand resizeCmd =
                new ResizeVolumeCommand(vol.getPath(), new StorageFilerTO(pool), vol.getSize(), resizeParameter.newSize, resizeParameter.shrinkOk,
                        resizeParameter.instanceName);
        final CreateCmdResult result = new CreateCmdResult(null, null);
        try {
            final ResizeVolumeAnswer answer = (ResizeVolumeAnswer) storageMgr.sendToPool(pool, resizeParameter.hosts, resizeCmd);
            if (answer != null && answer.getResult()) {
                final long finalSize = answer.getNewSize();
                s_logger.debug("Resize: volume started at size " + vol.getSize() + " and ended at size " + finalSize);

                vol.setSize(finalSize);
                vol.update();
            } else if (answer != null) {
                result.setResult(answer.getDetails());
            } else {
                s_logger.debug("return a null answer, mark it as failed for unknown reason");
                result.setResult("return a null answer, mark it as failed for unknown reason");
            }
        } catch (final Exception e) {
            s_logger.debug("sending resize command failed", e);
            result.setResult(e.toString());
        }

        callback.complete(result);
    }

    @Override
    public ChapInfo getChapInfo(final VolumeInfo volumeInfo) {
        return null;
    }

    @Override
    public boolean grantAccess(final DataObject dataObject, final Host host, final DataStore dataStore) {
        return false;
    }

    @Override
    public void revokeAccess(final DataObject dataObject, final Host host, final DataStore dataStore) {
    }

    @Override
    public long getVolumeSizeIncludingHypervisorSnapshotReserve(final Volume volume, final StoragePool pool) {
        return volume.getSize();
    }

    @Override
    public long getUsedBytes(final StoragePool storagePool) {
        return 0;
    }

    @Override
    public long getUsedIops(final StoragePool storagePool) {
        return 0;
    }

    @Override
    public void takeSnapshot(final SnapshotInfo snapshot, final AsyncCompletionCallback<CreateCmdResult> callback) {
        CreateCmdResult result = null;
        try {
            final SnapshotObjectTO snapshotTO = (SnapshotObjectTO) snapshot.getTO();
            final Object payload = snapshot.getPayload();
            if (payload != null && payload instanceof CreateSnapshotPayload) {
                final CreateSnapshotPayload snapshotPayload = (CreateSnapshotPayload) payload;
                snapshotTO.setQuiescevm(snapshotPayload.getQuiescevm());
            }

            final CreateObjectCommand cmd = new CreateObjectCommand(snapshotTO);
            final EndPoint ep = epSelector.select(snapshot, StorageAction.TAKESNAPSHOT);
            Answer answer = null;

            if (ep == null) {
                final String errMsg = "No remote endpoint to send createObjectCommand, check if host or ssvm is down?";
                s_logger.error(errMsg);
                answer = new Answer(cmd, false, errMsg);
            } else {
                answer = ep.sendMessage(cmd);
            }

            result = new CreateCmdResult(null, answer);
            if (answer != null && !answer.getResult()) {
                result.setResult(answer.getDetails());
            }

            callback.complete(result);
            return;
        } catch (final Exception e) {
            s_logger.debug("Failed to take snapshot: " + snapshot.getId(), e);
            result = new CreateCmdResult(null, null);
            result.setResult(e.toString());
        }
        callback.complete(result);
    }

    @Override
    public void revertSnapshot(final SnapshotInfo snapshot, final SnapshotInfo snapshotOnPrimaryStore, final AsyncCompletionCallback<CommandResult> callback) {
        final SnapshotObjectTO snapshotTO = (SnapshotObjectTO) snapshot.getTO();
        final RevertSnapshotCommand cmd = new RevertSnapshotCommand(snapshotTO);

        final CommandResult result = new CommandResult();
        try {
            final EndPoint ep = epSelector.select(snapshotOnPrimaryStore);
            if (ep == null) {
                final String errMsg = "No remote endpoint to send RevertSnapshotCommand, check if host or ssvm is down?";
                s_logger.error(errMsg);
                result.setResult(errMsg);
            } else {
                final Answer answer = ep.sendMessage(cmd);
                if (answer != null && !answer.getResult()) {
                    result.setResult(answer.getDetails());
                }
            }
        } catch (final Exception ex) {
            s_logger.debug("Unable to revert snapshot " + snapshot.getId(), ex);
            result.setResult(ex.toString());
        }
        callback.complete(result);
    }
}
