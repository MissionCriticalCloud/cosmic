package com.cloud.storage.dao;

import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Snapshot;
import com.cloud.storage.Snapshot.Type;
import com.cloud.storage.SnapshotVO;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;

import java.util.List;

public interface SnapshotDao extends GenericDao<SnapshotVO, Long>, StateDao<Snapshot.State, Snapshot.Event, SnapshotVO> {
    List<SnapshotVO> listByVolumeId(long volumeId);

    List<SnapshotVO> listByVolumeId(Filter filter, long volumeId);

    SnapshotVO findNextSnapshot(long parentSnapId);

    long getLastSnapshot(long volumeId, DataStoreRole role);

    List<SnapshotVO> listByVolumeIdType(long volumeId, Type type);

    List<SnapshotVO> listByVolumeIdIncludingRemoved(long volumeId);

    List<SnapshotVO> listByBackupUuid(long volumeId, String backupUuid);

    long updateSnapshotVersion(long volumeId, String from, String to);

    List<SnapshotVO> listByVolumeIdVersion(long volumeId, String version);

    Long getSecHostId(long volumeId);

    long updateSnapshotSecHost(long dcId, long secHostId);

    public Long countSnapshotsForAccount(long accountId);

    List<SnapshotVO> listByInstanceId(long instanceId, Snapshot.State... status);

    List<SnapshotVO> listByStatus(long volumeId, Snapshot.State... status);

    List<SnapshotVO> listAllByStatus(Snapshot.State... status);

    void updateVolumeIds(long oldVolId, long newVolId);
}
