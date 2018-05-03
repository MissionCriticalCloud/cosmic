package com.cloud.engine.subsystem.api.storage;

import com.cloud.framework.async.AsyncCompletionCallback;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.storage.StoragePool;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.storage.command.CommandResult;

public interface PrimaryDataStoreDriver extends DataStoreDriver {
    ChapInfo getChapInfo(VolumeInfo volumeInfo);

    boolean grantAccess(DataObject dataObject, Host host, DataStore dataStore);

    void revokeAccess(DataObject dataObject, Host host, DataStore dataStore);

    // intended for managed storage (cloud.storage_pool.managed = true)
    // if not managed, return volume.getSize()
    long getVolumeSizeIncludingHypervisorSnapshotReserve(Volume volume, StoragePool storagePool);

    // intended for managed storage (cloud.storage_pool.managed = true)
    // if managed storage, return the total number of bytes currently in use for the storage pool in question
    // if not managed storage, return 0
    long getUsedBytes(StoragePool storagePool);

    // intended for managed storage (cloud.storage_pool.managed = true)
    // if managed storage, return the total number of IOPS currently in use for the storage pool in question
    // if not managed storage, return 0
    long getUsedIops(StoragePool storagePool);

    void takeSnapshot(SnapshotInfo snapshot, AsyncCompletionCallback<CreateCmdResult> callback);

    void revertSnapshot(SnapshotInfo snapshotOnImageStore, SnapshotInfo snapshotOnPrimaryStore, AsyncCompletionCallback<CommandResult> callback);
}
