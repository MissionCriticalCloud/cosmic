package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.storage.Snapshot;

public interface SnapshotStrategy {
    SnapshotInfo takeSnapshot(SnapshotInfo snapshot);

    SnapshotInfo backupSnapshot(SnapshotInfo snapshot);

    boolean deleteSnapshot(Long snapshotId);

    boolean revertSnapshot(SnapshotInfo snapshot);

    StrategyPriority canHandle(Snapshot snapshot, SnapshotOperation op);

    enum SnapshotOperation {
        TAKE, BACKUP, DELETE, REVERT
    }
}
