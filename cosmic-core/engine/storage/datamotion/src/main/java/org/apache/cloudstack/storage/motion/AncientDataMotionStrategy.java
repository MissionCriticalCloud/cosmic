package org.apache.cloudstack.storage.motion;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.storage.MigrateVolumeAnswer;
import com.cloud.agent.api.storage.MigrateVolumeCommand;
import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.configuration.Config;
import com.cloud.host.Host;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StoragePool;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.DB;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.VirtualMachineManager;
import org.apache.cloudstack.engine.subsystem.api.storage.ClusterScope;
import org.apache.cloudstack.engine.subsystem.api.storage.CopyCommandResult;
import org.apache.cloudstack.engine.subsystem.api.storage.DataMotionStrategy;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPointSelector;
import org.apache.cloudstack.engine.subsystem.api.storage.HostScope;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import org.apache.cloudstack.engine.subsystem.api.storage.Scope;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.StorageAction;
import org.apache.cloudstack.engine.subsystem.api.storage.StorageCacheManager;
import org.apache.cloudstack.engine.subsystem.api.storage.StrategyPriority;
import org.apache.cloudstack.engine.subsystem.api.storage.TemplateInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.ZoneScope;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.storage.RemoteHostEndPoint;
import org.apache.cloudstack.storage.command.CopyCommand;
import org.apache.cloudstack.storage.image.datastore.ImageStoreEntity;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AncientDataMotionStrategy implements DataMotionStrategy {
    private static final Logger s_logger = LoggerFactory.getLogger(AncientDataMotionStrategy.class);
    @Inject
    EndPointSelector selector;
    @Inject
    ConfigurationDao configDao;
    @Inject
    VolumeDao volDao;
    @Inject
    DataStoreManager dataStoreMgr;
    @Inject
    StorageCacheManager cacheMgr;

    @Override
    public StrategyPriority canHandle(final DataObject srcData, final DataObject destData) {
        return StrategyPriority.DEFAULT;
    }

    @Override
    public StrategyPriority canHandle(final Map<VolumeInfo, DataStore> volumeMap, final Host srcHost, final Host destHost) {
        return StrategyPriority.CANT_HANDLE;
    }

    // Note: destHost is currently only used if the copyObject method is invoked
    @Override
    public Void copyAsync(final DataObject srcData, final DataObject destData, final Host destHost, final AsyncCompletionCallback<CopyCommandResult> callback) {
        Answer answer = null;
        String errMsg = null;
        try {
            s_logger.debug("copyAsync inspecting src type " + srcData.getType().toString() + " copyAsync inspecting dest type " + destData.getType().toString());

            if (srcData.getType() == DataObjectType.SNAPSHOT && destData.getType() == DataObjectType.VOLUME) {
                answer = copyVolumeFromSnapshot(srcData, destData);
            } else if (srcData.getType() == DataObjectType.SNAPSHOT && destData.getType() == DataObjectType.TEMPLATE) {
                answer = createTemplateFromSnapshot(srcData, destData);
            } else if (srcData.getType() == DataObjectType.TEMPLATE && destData.getType() == DataObjectType.VOLUME) {
                answer = cloneVolume(srcData, destData);
            } else if (destData.getType() == DataObjectType.VOLUME && srcData.getType() == DataObjectType.VOLUME &&
                    srcData.getDataStore().getRole() == DataStoreRole.Primary && destData.getDataStore().getRole() == DataStoreRole.Primary) {
                if (srcData.getId() == destData.getId()) {
                    // The volume has to be migrated across storage pools.
                    answer = migrateVolumeToPool(srcData, destData);
                } else {
                    answer = copyVolumeBetweenPools(srcData, destData);
                }
            } else if (srcData.getType() == DataObjectType.SNAPSHOT && destData.getType() == DataObjectType.SNAPSHOT) {
                answer = copySnapshot(srcData, destData);
            } else {
                answer = copyObject(srcData, destData, destHost);
            }

            if (answer != null && !answer.getResult()) {
                errMsg = answer.getDetails();
            }
        } catch (final Exception e) {
            s_logger.debug("copy failed", e);
            errMsg = e.toString();
        }
        final CopyCommandResult result = new CopyCommandResult(null, answer);
        result.setResult(errMsg);
        callback.complete(result);
        return null;
    }

    @Override
    public Void copyAsync(final DataObject srcData, final DataObject destData, final AsyncCompletionCallback<CopyCommandResult> callback) {
        return copyAsync(srcData, destData, null, callback);
    }

    @Override
    public Void copyAsync(final Map<VolumeInfo, DataStore> volumeMap, final VirtualMachineTO vmTo, final Host srcHost, final Host destHost, final
    AsyncCompletionCallback<CopyCommandResult> callback) {
        final CopyCommandResult result = new CopyCommandResult(null, null);
        result.setResult("Unsupported operation requested for copying data.");
        callback.complete(result);

        return null;
    }

    protected boolean needCacheStorage(final DataObject srcData, final DataObject destData) {
        final DataTO srcTO = srcData.getTO();
        final DataStoreTO srcStoreTO = srcTO.getDataStore();

        if (srcStoreTO instanceof NfsTO || srcStoreTO.getRole() == DataStoreRole.ImageCache) {
            //||
            //    (srcStoreTO instanceof PrimaryDataStoreTO && ((PrimaryDataStoreTO)srcStoreTO).getPoolType() == StoragePoolType.NetworkFilesystem)) {
            return false;
        }

        final DataTO destTO = destData.getTO();
        final DataStoreTO destStoreTO = destTO.getDataStore();

        if (destStoreTO instanceof NfsTO || destStoreTO.getRole() == DataStoreRole.ImageCache) {
            return false;
        }

        if (srcData.getType() == DataObjectType.TEMPLATE) {
            final TemplateInfo template = (TemplateInfo) srcData;
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("needCacheStorage true, dest at " + destTO.getPath() + " dest role " + destStoreTO.getRole().toString() + srcTO.getPath() + " src role " +
                    srcStoreTO.getRole().toString());
        }
        return true;
    }

    private Scope getZoneScope(final Scope destScope) {
        ZoneScope zoneScope = null;
        if (destScope instanceof ClusterScope) {
            final ClusterScope clusterScope = (ClusterScope) destScope;
            zoneScope = new ZoneScope(clusterScope.getZoneId());
        } else if (destScope instanceof HostScope) {
            final HostScope hostScope = (HostScope) destScope;
            zoneScope = new ZoneScope(hostScope.getZoneId());
        } else {
            zoneScope = (ZoneScope) destScope;
        }
        return zoneScope;
    }

    private Scope pickCacheScopeForCopy(final DataObject srcData, final DataObject destData) {
        final Scope srcScope = srcData.getDataStore().getScope();
        final Scope destScope = destData.getDataStore().getScope();

        Scope selectedScope = null;
        if (srcScope.getScopeId() != null) {
            selectedScope = getZoneScope(srcScope);
        } else if (destScope.getScopeId() != null) {
            selectedScope = getZoneScope(destScope);
        } else {
            s_logger.warn("Cannot find a zone-wide scope for movement that needs a cache storage");
        }
        return selectedScope;
    }

    protected Answer copyObject(final DataObject srcData, final DataObject destData, final Host destHost) {
        final String value = configDao.getValue(Config.PrimaryStorageDownloadWait.toString());
        final int _primaryStorageDownloadWait = NumbersUtil.parseInt(value, Integer.parseInt(Config.PrimaryStorageDownloadWait.getDefaultValue()));
        Answer answer = null;
        DataObject cacheData = null;
        DataObject srcForCopy = srcData;
        try {
            if (needCacheStorage(srcData, destData)) {
                final Scope destScope = pickCacheScopeForCopy(srcData, destData);
                srcForCopy = cacheData = cacheMgr.createCacheObject(srcData, destScope);
            }

            final CopyCommand cmd = new CopyCommand(srcForCopy.getTO(), destData.getTO(), _primaryStorageDownloadWait, VirtualMachineManager.ExecuteInSequence.value());
            final EndPoint ep = destHost != null ? RemoteHostEndPoint.getHypervisorHostEndPoint(destHost) : selector.select(srcForCopy, destData);
            if (ep == null) {
                final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                s_logger.error(errMsg);
                answer = new Answer(cmd, false, errMsg);
            } else {
                answer = ep.sendMessage(cmd);
            }

            if (cacheData != null) {
                final Long cacheId = cacheData.getId();
                final String cacheType = cacheData.getType().toString();
                final String cacheUuid = cacheData.getUuid().toString();

                if (srcData.getType() == DataObjectType.VOLUME &&
                        (destData.getType() == DataObjectType.VOLUME ||
                                destData.getType() == DataObjectType.TEMPLATE)) {
                    // volume transfer from primary to secondary. Volume transfer between primary pools are already handled by copyVolumeBetweenPools
                    // Delete cache in order to certainly transfer a latest image.
                    s_logger.debug("Delete " + cacheType + " cache(id: " + cacheId +
                            ", uuid: " + cacheUuid + ")");
                    cacheMgr.deleteCacheObject(srcForCopy);
                } else {
                    // for template, we want to leave it on cache for performance reason
                    if ((answer == null || !answer.getResult()) && srcForCopy.getRefCount() < 2) {
                        // cache object created by this copy, not already there
                        s_logger.warn("Copy may not be handled correctly by agent(id: " + (ep != null ? ep.getId() : "\"unspecified\"") + ")." +
                                " Delete " + cacheType + " cache(id: " + cacheId +
                                ", uuid: " + cacheUuid + ")");
                        cacheMgr.deleteCacheObject(srcForCopy);
                    } else {
                        s_logger.debug("Decrease reference count of " + cacheType +
                                " cache(id: " + cacheId + ", uuid: " + cacheUuid + ")");
                        cacheMgr.releaseCacheObject(srcForCopy);
                    }
                }
            }
            return answer;
        } catch (final Exception e) {
            s_logger.debug("copy object failed: ", e);
            if (cacheData != null) {
                cacheMgr.deleteCacheObject(cacheData);
            }
            throw new CloudRuntimeException(e.toString());
        }
    }

    protected Answer copyObject(final DataObject srcData, final DataObject destData) {
        return copyObject(srcData, destData, null);
    }

    protected DataObject cacheSnapshotChain(SnapshotInfo snapshot, final Scope scope) {
        DataObject leafData = null;
        final DataStore store = cacheMgr.getCacheStorage(snapshot, scope);
        while (snapshot != null) {
            final DataObject cacheData = cacheMgr.createCacheObject(snapshot, store);
            if (leafData == null) {
                leafData = cacheData;
            }
            snapshot = snapshot.getParent();
        }
        return leafData;
    }

    protected void deleteSnapshotCacheChain(SnapshotInfo snapshot) {
        while (snapshot != null) {
            cacheMgr.deleteCacheObject(snapshot);
            snapshot = snapshot.getParent();
        }
    }

    protected void releaseSnapshotCacheChain(SnapshotInfo snapshot) {
        while (snapshot != null) {
            cacheMgr.releaseCacheObject(snapshot);
            snapshot = snapshot.getParent();
        }
    }

    protected Answer copyVolumeFromSnapshot(final DataObject snapObj, final DataObject volObj) {
        final SnapshotInfo snapshot = (SnapshotInfo) snapObj;
        final StoragePool pool = (StoragePool) volObj.getDataStore();

        final String basicErrMsg = "Failed to create volume from " + snapshot.getName() + " on pool " + pool;
        final DataStore store = snapObj.getDataStore();
        final DataStoreTO storTO = store.getTO();
        DataObject srcData = snapObj;
        try {
            if (!(storTO instanceof NfsTO)) {
                // cache snapshot to zone-wide staging store for the volume to be created
                srcData = cacheSnapshotChain(snapshot, new ZoneScope(pool.getDataCenterId()));
            }

            final String value = configDao.getValue(Config.CreateVolumeFromSnapshotWait.toString());
            final int _createVolumeFromSnapshotWait = NumbersUtil.parseInt(value, Integer.parseInt(Config.CreateVolumeFromSnapshotWait.getDefaultValue()));

            EndPoint ep = null;
            if (srcData.getDataStore().getRole() == DataStoreRole.Primary) {
                ep = selector.select(volObj);
            } else {
                ep = selector.select(srcData, volObj);
            }

            final CopyCommand cmd = new CopyCommand(srcData.getTO(), volObj.getTO(), _createVolumeFromSnapshotWait, VirtualMachineManager.ExecuteInSequence.value());
            Answer answer = null;
            if (ep == null) {
                final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                s_logger.error(errMsg);
                answer = new Answer(cmd, false, errMsg);
            } else {
                answer = ep.sendMessage(cmd);
            }

            return answer;
        } catch (final Exception e) {
            s_logger.error(basicErrMsg, e);
            throw new CloudRuntimeException(basicErrMsg);
        } finally {
            if (!(storTO instanceof NfsTO)) {
                // still keep snapshot on cache which may be migrated from previous secondary storage
                releaseSnapshotCacheChain((SnapshotInfo) srcData);
            }
        }
    }

    protected Answer cloneVolume(final DataObject template, final DataObject volume) {
        final CopyCommand cmd = new CopyCommand(template.getTO(), volume.getTO(), 0, VirtualMachineManager.ExecuteInSequence.value());
        try {
            final EndPoint ep = selector.select(volume.getDataStore());
            Answer answer = null;
            if (ep == null) {
                final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                s_logger.error(errMsg);
                answer = new Answer(cmd, false, errMsg);
            } else {
                answer = ep.sendMessage(cmd);
            }
            return answer;
        } catch (final Exception e) {
            s_logger.debug("Failed to send to storage pool", e);
            throw new CloudRuntimeException("Failed to send to storage pool", e);
        }
    }

    protected Answer copyVolumeBetweenPools(final DataObject srcData, final DataObject destData) {
        final String value = configDao.getValue(Config.CopyVolumeWait.key());
        final int _copyvolumewait = NumbersUtil.parseInt(value, Integer.parseInt(Config.CopyVolumeWait.getDefaultValue()));

        final Scope destScope = getZoneScope(destData.getDataStore().getScope());
        final DataStore cacheStore = cacheMgr.getCacheStorage(destScope);
        if (cacheStore == null) {
            // need to find a nfs or cifs image store, assuming that can't copy volume
            // directly to s3
            final ImageStoreEntity imageStore = (ImageStoreEntity) dataStoreMgr.getImageStore(destScope.getScopeId());
            if (!imageStore.getProtocol().equalsIgnoreCase("nfs") && !imageStore.getProtocol().equalsIgnoreCase("cifs")) {
                s_logger.debug("can't find a nfs (or cifs) image store to satisfy the need for a staging store");
                return null;
            }

            final DataObject objOnImageStore = imageStore.create(srcData);
            objOnImageStore.processEvent(Event.CreateOnlyRequested);

            Answer answer = copyObject(srcData, objOnImageStore);
            if (answer == null || !answer.getResult()) {
                if (answer != null) {
                    s_logger.debug("copy to image store failed: " + answer.getDetails());
                }
                objOnImageStore.processEvent(Event.OperationFailed);
                imageStore.delete(objOnImageStore);
                return answer;
            }

            objOnImageStore.processEvent(Event.OperationSuccessed, answer);

            objOnImageStore.processEvent(Event.CopyingRequested);

            final CopyCommand cmd = new CopyCommand(objOnImageStore.getTO(), destData.getTO(), _copyvolumewait, VirtualMachineManager.ExecuteInSequence.value());
            final EndPoint ep = selector.select(objOnImageStore, destData);
            if (ep == null) {
                final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                s_logger.error(errMsg);
                answer = new Answer(cmd, false, errMsg);
            } else {
                answer = ep.sendMessage(cmd);
            }

            if (answer == null || !answer.getResult()) {
                if (answer != null) {
                    s_logger.debug("copy to primary store failed: " + answer.getDetails());
                }
                objOnImageStore.processEvent(Event.OperationFailed);
                imageStore.delete(objOnImageStore);
                return answer;
            }

            objOnImageStore.processEvent(Event.OperationSuccessed);
            imageStore.delete(objOnImageStore);
            return answer;
        } else {
            final DataObject cacheData = cacheMgr.createCacheObject(srcData, destScope);
            final CopyCommand cmd = new CopyCommand(cacheData.getTO(), destData.getTO(), _copyvolumewait, VirtualMachineManager.ExecuteInSequence.value());
            final EndPoint ep = selector.select(cacheData, destData);
            Answer answer = null;
            if (ep == null) {
                final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                s_logger.error(errMsg);
                answer = new Answer(cmd, false, errMsg);
            } else {
                answer = ep.sendMessage(cmd);
            }
            // delete volume on cache store
            if (cacheData != null) {
                cacheMgr.deleteCacheObject(cacheData);
            }
            return answer;
        }
    }

    protected Answer migrateVolumeToPool(final DataObject srcData, final DataObject destData) {
        final String value = configDao.getValue(Config.MigrateWait.key());
        final int waitInterval = NumbersUtil.parseInt(value, Integer.parseInt(Config.MigrateWait.getDefaultValue()));

        final VolumeInfo volume = (VolumeInfo) srcData;
        final StoragePool destPool = (StoragePool) dataStoreMgr.getDataStore(destData.getDataStore().getId(), DataStoreRole.Primary);
        final MigrateVolumeCommand command = new MigrateVolumeCommand(volume.getId(), volume.getPath(), destPool, volume.getAttachedVmName(), volume.getVolumeType(), waitInterval);
        final EndPoint ep = selector.select(srcData, StorageAction.MIGRATEVOLUME);
        Answer answer = null;
        if (ep == null) {
            final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
            s_logger.error(errMsg);
            answer = new Answer(command, false, errMsg);
        } else {
            answer = ep.sendMessage(command);
        }

        if (answer == null || !answer.getResult()) {
            throw new CloudRuntimeException("Failed to migrate volume " + volume + " to storage pool " + destPool);
        } else {
            // Update the volume details after migration.
            final VolumeVO volumeVo = volDao.findById(volume.getId());
            final Long oldPoolId = volume.getPoolId();
            volumeVo.setPath(((MigrateVolumeAnswer) answer).getVolumePath());
            final String chainInfo = ((MigrateVolumeAnswer) answer).getVolumeChainInfo();
            if (chainInfo != null) {
                volumeVo.setChainInfo(chainInfo);
            }
            volumeVo.setPodId(destPool.getPodId());
            volumeVo.setPoolId(destPool.getId());
            volumeVo.setLastPoolId(oldPoolId);
            // For SMB, pool credentials are also stored in the uri query string.  We trim the query string
            // part  here to make sure the credentials do not get stored in the db unencrypted.
            String folder = destPool.getPath();
            if (destPool.getPoolType() == StoragePoolType.SMB && folder != null && folder.contains("?")) {
                folder = folder.substring(0, folder.indexOf("?"));
            }
            volumeVo.setFolder(folder);
            volDao.update(volume.getId(), volumeVo);
        }

        return answer;
    }

    @DB
    protected Answer createTemplateFromSnapshot(DataObject srcData, final DataObject destData) {

        final String value = configDao.getValue(Config.CreatePrivateTemplateFromSnapshotWait.toString());
        final int _createprivatetemplatefromsnapshotwait = NumbersUtil.parseInt(value, Integer.parseInt(Config.CreatePrivateTemplateFromSnapshotWait.getDefaultValue()));

        boolean needCache = false;
        if (needCacheStorage(srcData, destData)) {
            needCache = true;
            final SnapshotInfo snapshot = (SnapshotInfo) srcData;
            srcData = cacheSnapshotChain(snapshot, snapshot.getDataStore().getScope());
        }

        EndPoint ep = null;
        if (srcData.getDataStore().getRole() == DataStoreRole.Primary) {
            ep = selector.select(destData);
        } else {
            ep = selector.select(srcData, destData);
        }

        final CopyCommand cmd = new CopyCommand(srcData.getTO(), destData.getTO(), _createprivatetemplatefromsnapshotwait, VirtualMachineManager.ExecuteInSequence.value());
        Answer answer = null;
        if (ep == null) {
            final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
            s_logger.error(errMsg);
            answer = new Answer(cmd, false, errMsg);
        } else {
            answer = ep.sendMessage(cmd);
        }

        // clean up snapshot copied to staging
        if (needCache && srcData != null) {
            cacheMgr.releaseCacheObject(srcData);  // reduce ref count, but keep it there on cache which is converted from previous secondary storage
        }
        return answer;
    }

    protected Answer copySnapshot(final DataObject srcData, final DataObject destData) {
        final String value = configDao.getValue(Config.BackupSnapshotWait.toString());
        final int _backupsnapshotwait = NumbersUtil.parseInt(value, Integer.parseInt(Config.BackupSnapshotWait.getDefaultValue()));

        DataObject cacheData = null;
        final SnapshotInfo snapshotInfo = (SnapshotInfo) srcData;
        final Object payload = snapshotInfo.getPayload();
        Boolean fullSnapshot = true;
        if (payload != null) {
            fullSnapshot = (Boolean) payload;
        }
        final Map<String, String> options = new HashMap<>();
        options.put("fullSnapshot", fullSnapshot.toString());
        Answer answer = null;
        try {
            if (needCacheStorage(srcData, destData)) {
                final Scope selectedScope = pickCacheScopeForCopy(srcData, destData);
                cacheData = cacheMgr.getCacheObject(srcData, selectedScope);

                final CopyCommand cmd = new CopyCommand(srcData.getTO(), destData.getTO(), _backupsnapshotwait, VirtualMachineManager.ExecuteInSequence.value());
                cmd.setCacheTO(cacheData.getTO());
                cmd.setOptions(options);
                final EndPoint ep = selector.select(srcData, destData);
                if (ep == null) {
                    final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                    s_logger.error(errMsg);
                    answer = new Answer(cmd, false, errMsg);
                } else {
                    answer = ep.sendMessage(cmd);
                }
            } else {
                final CopyCommand cmd = new CopyCommand(srcData.getTO(), destData.getTO(), _backupsnapshotwait, VirtualMachineManager.ExecuteInSequence.value());
                cmd.setOptions(options);
                final EndPoint ep = selector.select(srcData, destData, StorageAction.BACKUPSNAPSHOT);
                if (ep == null) {
                    final String errMsg = "No remote endpoint to send command, check if host or ssvm is down?";
                    s_logger.error(errMsg);
                    answer = new Answer(cmd, false, errMsg);
                } else {
                    answer = ep.sendMessage(cmd);
                }
            }
            // clean up cache entry
            if (cacheData != null) {
                cacheMgr.deleteCacheObject(cacheData);
            }
            return answer;
        } catch (final Exception e) {
            s_logger.debug("copy snasphot failed: " + e.toString());
            if (cacheData != null) {
                cacheMgr.deleteCacheObject(cacheData);
            }
            throw new CloudRuntimeException(e.toString());
        }
    }
}
