package org.apache.cloudstack.engine.subsystem.api.storage;

public interface SnapshotService {
    SnapshotResult takeSnapshot(SnapshotInfo snapshot);

    SnapshotInfo backupSnapshot(SnapshotInfo snapshot);

    boolean deleteSnapshot(SnapshotInfo snapshot);

    boolean revertSnapshot(SnapshotInfo snapshot);

    void syncVolumeSnapshotsToRegionStore(long volumeId, DataStore store);

    void cleanupVolumeDuringSnapshotFailure(Long volumeId, Long snapshotId);
}
