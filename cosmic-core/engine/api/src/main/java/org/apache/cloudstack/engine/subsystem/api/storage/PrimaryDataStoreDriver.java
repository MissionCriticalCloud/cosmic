package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.host.Host;
import com.cloud.storage.StoragePool;
import com.cloud.storage.Volume;
import com.cloud.storage.command.CommandResult;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;

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
