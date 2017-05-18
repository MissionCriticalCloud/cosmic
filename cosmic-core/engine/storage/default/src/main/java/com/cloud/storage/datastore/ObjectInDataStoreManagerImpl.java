package com.cloud.storage.datastore;

import com.cloud.agent.api.to.DataObjectType;
import com.cloud.engine.subsystem.api.storage.DataObject;
import com.cloud.engine.subsystem.api.storage.DataObjectInStore;
import com.cloud.engine.subsystem.api.storage.DataStore;
import com.cloud.engine.subsystem.api.storage.DataStoreManager;
import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;
import com.cloud.engine.subsystem.api.storage.SnapshotDataFactory;
import com.cloud.engine.subsystem.api.storage.SnapshotInfo;
import com.cloud.engine.subsystem.api.storage.TemplateDataFactory;
import com.cloud.engine.subsystem.api.storage.TemplateInfo;
import com.cloud.engine.subsystem.api.storage.VolumeDataFactory;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.VMTemplateStoragePoolVO;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplatePoolDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.SnapshotDataStoreDao;
import com.cloud.storage.datastore.db.SnapshotDataStoreVO;
import com.cloud.storage.datastore.db.TemplateDataStoreDao;
import com.cloud.storage.datastore.db.TemplateDataStoreVO;
import com.cloud.storage.datastore.db.VolumeDataStoreDao;
import com.cloud.storage.datastore.db.VolumeDataStoreVO;
import com.cloud.storage.db.ObjectInDataStoreDao;
import com.cloud.storage.template.TemplateConstants;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.fsm.NoTransitionException;
import com.cloud.utils.fsm.StateMachine2;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ObjectInDataStoreManagerImpl implements ObjectInDataStoreManager {
    private static final Logger s_logger = LoggerFactory.getLogger(ObjectInDataStoreManagerImpl.class);
    protected StateMachine2<State, Event, DataObjectInStore> stateMachines;
    @Inject
    TemplateDataFactory imageFactory;
    @Inject
    DataStoreManager storeMgr;
    @Inject
    VolumeDataFactory volumeFactory;
    @Inject
    TemplateDataStoreDao templateDataStoreDao;
    @Inject
    SnapshotDataStoreDao snapshotDataStoreDao;
    @Inject
    VolumeDataStoreDao volumeDataStoreDao;
    @Inject
    VMTemplatePoolDao templatePoolDao;
    @Inject
    SnapshotDataFactory snapshotFactory;
    @Inject
    ObjectInDataStoreDao objInStoreDao;
    @Inject
    VMTemplateDao templateDao;
    @Inject
    SnapshotDao snapshotDao;
    @Inject
    VolumeDao volumeDao;

    public ObjectInDataStoreManagerImpl() {
        stateMachines = new StateMachine2<>();
        stateMachines.addTransition(State.Allocated, Event.CreateOnlyRequested, State.Creating);
        stateMachines.addTransition(State.Allocated, Event.DestroyRequested, State.Destroying);
        stateMachines.addTransition(State.Allocated, Event.OperationFailed, State.Failed);
        stateMachines.addTransition(State.Creating, Event.OperationFailed, State.Allocated);
        stateMachines.addTransition(State.Creating, Event.OperationSuccessed, State.Ready);
        stateMachines.addTransition(State.Ready, Event.CopyingRequested, State.Copying);
        stateMachines.addTransition(State.Copying, Event.OperationSuccessed, State.Ready);
        stateMachines.addTransition(State.Copying, Event.OperationFailed, State.Ready);
        stateMachines.addTransition(State.Ready, Event.DestroyRequested, State.Destroying);
        stateMachines.addTransition(State.Destroying, Event.DestroyRequested, State.Destroying);
        stateMachines.addTransition(State.Destroying, Event.OperationSuccessed, State.Destroyed);
        stateMachines.addTransition(State.Destroying, Event.OperationFailed, State.Destroying);
        stateMachines.addTransition(State.Failed, Event.DestroyRequested, State.Destroying);
        // TODO: further investigate why an extra event is sent when it is
        // alreay Ready for DownloadListener
        stateMachines.addTransition(State.Ready, Event.OperationSuccessed, State.Ready);
    }

    @Override
    public DataObject create(final DataObject obj, final DataStore dataStore) {
        if (dataStore.getRole() == DataStoreRole.Primary) {
            if (obj.getType() == DataObjectType.TEMPLATE) {
                VMTemplateStoragePoolVO vo = new VMTemplateStoragePoolVO(dataStore.getId(), obj.getId());
                vo = templatePoolDao.persist(vo);
            } else if (obj.getType() == DataObjectType.SNAPSHOT) {
                final SnapshotInfo snapshotInfo = (SnapshotInfo) obj;
                SnapshotDataStoreVO ss = new SnapshotDataStoreVO();
                ss.setSnapshotId(obj.getId());
                ss.setDataStoreId(dataStore.getId());
                ss.setRole(dataStore.getRole());
                ss.setVolumeId(snapshotInfo.getVolumeId());
                ss.setSize(snapshotInfo.getSize()); // this is the virtual size of snapshot in primary storage.
                ss.setPhysicalSize(snapshotInfo.getSize()); // this physical size will get updated with actual size once the snapshot backup is done.
                final SnapshotDataStoreVO snapshotDataStoreVO = snapshotDataStoreDao.findParent(dataStore.getRole(), dataStore.getId(), snapshotInfo.getVolumeId());
                if (snapshotDataStoreVO != null) {
                    //Double check the snapshot is removed or not
                    final SnapshotVO parentSnap = snapshotDao.findById(snapshotDataStoreVO.getSnapshotId());
                    if (parentSnap != null) {
                        ss.setParentSnapshotId(snapshotDataStoreVO.getSnapshotId());
                    } else {
                        s_logger.debug("find inconsistent db for snapshot " + snapshotDataStoreVO.getSnapshotId());
                    }
                }
                ss.setState(ObjectInDataStoreStateMachine.State.Allocated);
                ss = snapshotDataStoreDao.persist(ss);
            }
        } else {
            // Image store
            switch (obj.getType()) {
                case TEMPLATE:
                    TemplateDataStoreVO ts = new TemplateDataStoreVO();
                    ts.setTemplateId(obj.getId());
                    ts.setDataStoreId(dataStore.getId());
                    ts.setDataStoreRole(dataStore.getRole());
                    String installPath =
                            TemplateConstants.DEFAULT_TMPLT_ROOT_DIR + "/" + TemplateConstants.DEFAULT_TMPLT_FIRST_LEVEL_DIR +
                                    templateDao.findById(obj.getId()).getAccountId() + "/" + obj.getId();
                    ts.setInstallPath(installPath);
                    ts.setState(ObjectInDataStoreStateMachine.State.Allocated);
                    ts = templateDataStoreDao.persist(ts);
                    break;
                case SNAPSHOT:
                    final SnapshotInfo snapshot = (SnapshotInfo) obj;
                    SnapshotDataStoreVO ss = new SnapshotDataStoreVO();
                    ss.setSnapshotId(obj.getId());
                    ss.setDataStoreId(dataStore.getId());
                    ss.setRole(dataStore.getRole());
                    ss.setSize(snapshot.getSize());
                    ss.setVolumeId(snapshot.getVolumeId());
                    final SnapshotDataStoreVO snapshotDataStoreVO = snapshotDataStoreDao.findParent(dataStore.getRole(), dataStore.getId(), snapshot.getVolumeId());
                    if (snapshotDataStoreVO != null) {
                        ss.setParentSnapshotId(snapshotDataStoreVO.getSnapshotId());
                    }
                    ss.setInstallPath(TemplateConstants.DEFAULT_SNAPSHOT_ROOT_DIR + "/" + snapshotDao.findById(obj.getId()).getAccountId() + "/" + snapshot.getVolumeId());
                    ss.setState(ObjectInDataStoreStateMachine.State.Allocated);
                    ss = snapshotDataStoreDao.persist(ss);
                    break;
                case VOLUME:
                    VolumeDataStoreVO vs = new VolumeDataStoreVO();
                    vs.setVolumeId(obj.getId());
                    vs.setDataStoreId(dataStore.getId());
                    vs.setInstallPath(TemplateConstants.DEFAULT_VOLUME_ROOT_DIR + "/" + volumeDao.findById(obj.getId()).getAccountId() + "/" + obj.getId());
                    vs.setState(ObjectInDataStoreStateMachine.State.Allocated);
                    vs = volumeDataStoreDao.persist(vs);
                    break;
            }
        }

        return this.get(obj, dataStore);
    }

    @Override
    public boolean delete(final DataObject dataObj) {
        final long objId = dataObj.getId();
        final DataStore dataStore = dataObj.getDataStore();
        if (dataStore.getRole() == DataStoreRole.Primary) {
            if (dataObj.getType() == DataObjectType.TEMPLATE) {
                final VMTemplateStoragePoolVO destTmpltPool = templatePoolDao.findByPoolTemplate(dataStore.getId(), objId);
                if (destTmpltPool != null) {
                    return templatePoolDao.remove(destTmpltPool.getId());
                } else {
                    s_logger.warn("Template " + objId + " is not found on storage pool " + dataStore.getId() + ", so no need to delete");
                    return true;
                }
            }
        } else {
            // Image store
            switch (dataObj.getType()) {
                case TEMPLATE:
                    final TemplateDataStoreVO destTmpltStore = templateDataStoreDao.findByStoreTemplate(dataStore.getId(), objId);
                    if (destTmpltStore != null) {
                        return templateDataStoreDao.remove(destTmpltStore.getId());
                    } else {
                        s_logger.warn("Template " + objId + " is not found on image store " + dataStore.getId() + ", so no need to delete");
                        return true;
                    }
                case SNAPSHOT:
                    final SnapshotDataStoreVO destSnapshotStore = snapshotDataStoreDao.findByStoreSnapshot(dataStore.getRole(), dataStore.getId(), objId);
                    if (destSnapshotStore != null) {
                        return snapshotDataStoreDao.remove(destSnapshotStore.getId());
                    } else {
                        s_logger.warn("Snapshot " + objId + " is not found on image store " + dataStore.getId() + ", so no need to delete");
                        return true;
                    }
                case VOLUME:
                    final VolumeDataStoreVO destVolumeStore = volumeDataStoreDao.findByStoreVolume(dataStore.getId(), objId);
                    if (destVolumeStore != null) {
                        return volumeDataStoreDao.remove(destVolumeStore.getId());
                    } else {
                        s_logger.warn("Volume " + objId + " is not found on image store " + dataStore.getId() + ", so no need to delete");
                        return true;
                    }
            }
        }

        s_logger.warn("Unsupported data object (" + dataObj.getType() + ", " + dataObj.getDataStore() + ")");
        return false;
    }

    @Override
    public boolean deleteIfNotReady(final DataObject dataObj) {
        final long objId = dataObj.getId();
        final DataStore dataStore = dataObj.getDataStore();
        if (dataStore.getRole() == DataStoreRole.Primary) {
            if (dataObj.getType() == DataObjectType.TEMPLATE) {
                final VMTemplateStoragePoolVO destTmpltPool = templatePoolDao.findByPoolTemplate(dataStore.getId(), objId);
                if (destTmpltPool != null && destTmpltPool.getState() != ObjectInDataStoreStateMachine.State.Ready) {
                    return templatePoolDao.remove(destTmpltPool.getId());
                } else {
                    s_logger.warn("Template " + objId + " is not found on storage pool " + dataStore.getId() + ", so no need to delete");
                    return true;
                }
            } else if (dataObj.getType() == DataObjectType.SNAPSHOT) {
                final SnapshotDataStoreVO destSnapshotStore = snapshotDataStoreDao.findByStoreSnapshot(dataStore.getRole(), dataStore.getId(), objId);
                if (destSnapshotStore != null && destSnapshotStore.getState() != ObjectInDataStoreStateMachine.State.Ready) {
                    snapshotDataStoreDao.remove(destSnapshotStore.getId());
                }
                return true;
            }
        } else {
            // Image store
            switch (dataObj.getType()) {
                case TEMPLATE:
                    return true;
                case SNAPSHOT:
                    final SnapshotDataStoreVO destSnapshotStore = snapshotDataStoreDao.findByStoreSnapshot(dataStore.getRole(), dataStore.getId(), objId);
                    if (destSnapshotStore != null && destSnapshotStore.getState() != ObjectInDataStoreStateMachine.State.Ready) {
                        return snapshotDataStoreDao.remove(destSnapshotStore.getId());
                    } else {
                        s_logger.warn("Snapshot " + objId + " is not found on image store " + dataStore.getId() + ", so no need to delete");
                        return true;
                    }
                case VOLUME:
                    final VolumeDataStoreVO destVolumeStore = volumeDataStoreDao.findByStoreVolume(dataStore.getId(), objId);
                    if (destVolumeStore != null && destVolumeStore.getState() != ObjectInDataStoreStateMachine.State.Ready) {
                        return volumeDataStoreDao.remove(destVolumeStore.getId());
                    } else {
                        s_logger.warn("Volume " + objId + " is not found on image store " + dataStore.getId() + ", so no need to delete");
                        return true;
                    }
            }
        }

        s_logger.warn("Unsupported data object (" + dataObj.getType() + ", " + dataObj.getDataStore() + "), no need to delete from object in store ref table");
        return false;
    }

    @Override
    public DataObject get(final DataObject dataObj, final DataStore store) {
        if (dataObj.getType() == DataObjectType.TEMPLATE) {
            return imageFactory.getTemplate(dataObj, store);
        } else if (dataObj.getType() == DataObjectType.VOLUME) {
            return volumeFactory.getVolume(dataObj, store);
        } else if (dataObj.getType() == DataObjectType.SNAPSHOT) {
            return snapshotFactory.getSnapshot(dataObj, store);
        }

        throw new CloudRuntimeException("unknown type");
    }

    @Override
    public boolean update(final DataObject data, final Event event) throws NoTransitionException, ConcurrentOperationException {
        final DataObjectInStore obj = this.findObject(data, data.getDataStore());
        if (obj == null) {
            throw new CloudRuntimeException("can't find mapping in ObjectInDataStore table for: " + data);
        }

        boolean result = true;
        if (data.getDataStore().getRole() == DataStoreRole.Image || data.getDataStore().getRole() == DataStoreRole.ImageCache) {
            switch (data.getType()) {
                case TEMPLATE:
                    result = this.stateMachines.transitTo(obj, event, null, templateDataStoreDao);
                    break;
                case SNAPSHOT:
                    result = this.stateMachines.transitTo(obj, event, null, snapshotDataStoreDao);
                    break;
                case VOLUME:
                    result = this.stateMachines.transitTo(obj, event, null, volumeDataStoreDao);
                    break;
            }
        } else if (data.getType() == DataObjectType.TEMPLATE && data.getDataStore().getRole() == DataStoreRole.Primary) {

            result = this.stateMachines.transitTo(obj, event, null, templatePoolDao);
        } else if (data.getType() == DataObjectType.SNAPSHOT && data.getDataStore().getRole() == DataStoreRole.Primary) {
            result = this.stateMachines.transitTo(obj, event, null, snapshotDataStoreDao);
        } else {
            throw new CloudRuntimeException("Invalid data or store type: " + data.getType() + " " + data.getDataStore().getRole());
        }

        if (!result) {
            throw new ConcurrentOperationException("Multiple threads are trying to update data object state, racing condition");
        }
        return true;
    }

    @Override
    public DataObjectInStore findObject(final long objId, final DataObjectType type, final long dataStoreId, final DataStoreRole role) {
        DataObjectInStore vo = null;
        if (role == DataStoreRole.Image || role == DataStoreRole.ImageCache) {
            switch (type) {
                case TEMPLATE:
                    vo = templateDataStoreDao.findByStoreTemplate(dataStoreId, objId);
                    break;
                case SNAPSHOT:
                    vo = snapshotDataStoreDao.findByStoreSnapshot(role, dataStoreId, objId);
                    break;
                case VOLUME:
                    vo = volumeDataStoreDao.findByStoreVolume(dataStoreId, objId);
                    break;
            }
        } else if (type == DataObjectType.TEMPLATE && role == DataStoreRole.Primary) {
            vo = templatePoolDao.findByPoolTemplate(dataStoreId, objId);
        } else if (type == DataObjectType.SNAPSHOT && role == DataStoreRole.Primary) {
            vo = snapshotDataStoreDao.findByStoreSnapshot(role, dataStoreId, objId);
        } else {
            s_logger.debug("Invalid data or store type: " + type + " " + role);
            throw new CloudRuntimeException("Invalid data or store type: " + type + " " + role);
        }

        return vo;
    }

    @Override
    public DataObjectInStore findObject(final DataObject obj, final DataStore store) {
        return findObject(obj.getId(), obj.getType(), store.getId(), store.getRole());
    }
}
