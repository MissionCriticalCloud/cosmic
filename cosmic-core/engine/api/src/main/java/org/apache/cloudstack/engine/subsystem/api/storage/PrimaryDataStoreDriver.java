package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.host.Host;
import com.cloud.storage.StoragePool;
import com.cloud.storage.Volume;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.storage.command.CommandResult;

public interface PrimaryDataStoreDriver extends DataStoreDriver {
    public ChapInfo getChapInfo(VolumeInfo volumeInfo);

    public boolean grantAccess(DataObject dataObject, Host host, DataStore dataStore);

    public void revokeAccess(DataObject dataObject, Host host, DataStore dataStore);

    // intended for managed storage (cloud.storage_pool.managed = true)
    // if not managed, return volume.getSize()
    public long getVolumeSizeIncludingHypervisorSnapshotReserve(Volume volume, StoragePool storagePool);

    // intended for managed storage (cloud.storage_pool.managed = true)
    // if managed storage, return the total number of bytes currently in use for the storage pool in question
    // if not managed storage, return 0
    public long getUsedBytes(StoragePool storagePool);

    // intended for managed storage (cloud.storage_pool.managed = true)
    // if managed storage, return the total number of IOPS currently in use for the storage pool in question
    // if not managed storage, return 0
    public long getUsedIops(StoragePool storagePool);

    public void takeSnapshot(SnapshotInfo snapshot, AsyncCompletionCallback<CreateCmdResult> callback);

    public void revertSnapshot(SnapshotInfo snapshotOnImageStore, SnapshotInfo snapshotOnPrimaryStore, AsyncCompletionCallback<CommandResult> callback);
}
