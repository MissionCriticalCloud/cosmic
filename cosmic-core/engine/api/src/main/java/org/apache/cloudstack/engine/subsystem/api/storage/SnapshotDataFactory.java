package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.storage.DataStoreRole;

import java.util.List;

public interface SnapshotDataFactory {
    SnapshotInfo getSnapshot(long snapshotId, DataStore store);

    SnapshotInfo getSnapshot(DataObject obj, DataStore store);

    SnapshotInfo getSnapshot(long snapshotId, DataStoreRole role);

    List<SnapshotInfo> listSnapshotOnCache(long snapshotId);

    SnapshotInfo getReadySnapshotOnCache(long snapshotId);
}
