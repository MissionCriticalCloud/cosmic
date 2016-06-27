package org.apache.cloudstack.storage.volume;

public enum VolumeEvent {
    CreateRequested,
    CopyRequested,
    CopySucceeded,
    CopyFailed,
    OperationFailed,
    OperationSucceeded,
    OperationRetry,
    UploadRequested,
    MigrationRequested,
    SnapshotRequested,
    DestroyRequested,
    ExpungingRequested
}
