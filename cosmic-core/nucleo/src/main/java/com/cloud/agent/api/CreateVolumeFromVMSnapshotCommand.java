//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.vm.DiskProfile;

public class CreateVolumeFromVMSnapshotCommand extends Command {

    protected String path;
    protected String name;
    protected Boolean fullClone;
    protected String storagePoolUuid;
    private StorageFilerTO pool;
    private DiskProfile diskProfile;
    private Long volumeId;

    protected CreateVolumeFromVMSnapshotCommand() {

    }

    public CreateVolumeFromVMSnapshotCommand(final String path, final String name, final Boolean fullClone, final String storagePoolUuid) {
        this.path = path;
        this.name = name;
        this.fullClone = fullClone;
        this.storagePoolUuid = storagePoolUuid;
    }

    public CreateVolumeFromVMSnapshotCommand(final String path, final String name, final Boolean fullClone, final String storagePoolUuid, final StorageFilerTO pool, final
    DiskProfile diskProfile,
                                             final Long volumeId) {
        this.path = path;
        this.name = name;
        this.fullClone = fullClone;
        this.storagePoolUuid = storagePoolUuid;
        this.pool = pool;
        this.diskProfile = diskProfile;
        this.volumeId = volumeId;
    }

    public DiskProfile getDskch() {
        return diskProfile;
    }

    public String getPath() {
        return path;
    }

    public Long getVolumeId() {
        return volumeId;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getName() {
        return name;
    }

    public Boolean getFullClone() {
        return fullClone;
    }

    public String getStoragePoolUuid() {
        return storagePoolUuid;
    }

    public StorageFilerTO getPool() {
        return pool;
    }
}
