package org.apache.cloudstack.storage.datastore.db;

import com.cloud.storage.DataStoreRole;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;

import java.util.List;

public interface SnapshotDataStoreDao extends GenericDao<SnapshotDataStoreVO, Long>,
        StateDao<ObjectInDataStoreStateMachine.State, ObjectInDataStoreStateMachine.Event, DataObjectInStore> {

    List<SnapshotDataStoreVO> listByStoreId(long id, DataStoreRole role);

    List<SnapshotDataStoreVO> listActiveOnCache(long id);

    void deletePrimaryRecordsForStore(long id, DataStoreRole role);

    SnapshotDataStoreVO findByStoreSnapshot(DataStoreRole role, long storeId, long snapshotId);

    SnapshotDataStoreVO findParent(DataStoreRole role, Long storeId, Long volumeId);

    SnapshotDataStoreVO findBySnapshot(long snapshotId, DataStoreRole role);

    List<SnapshotDataStoreVO> listDestroyed(long storeId);

    List<SnapshotDataStoreVO> findBySnapshotId(long snapshotId);

    void duplicateCacheRecordsOnRegionStore(long storeId);

    // delete the snapshot entry on primary data store to make sure that next snapshot will be full snapshot
    void deleteSnapshotRecordsOnPrimary();

    SnapshotDataStoreVO findReadyOnCache(long snapshotId);

    List<SnapshotDataStoreVO> listOnCache(long snapshotId);

    void updateStoreRoleToCache(long storeId);

    SnapshotDataStoreVO findLatestSnapshotForVolume(Long volumeId, DataStoreRole role);

    SnapshotDataStoreVO findOldestSnapshotForVolume(Long volumeId, DataStoreRole role);

    void updateVolumeIds(long oldVolId, long newVolId);

    SnapshotDataStoreVO findByVolume(long volumeId, DataStoreRole role);
}
