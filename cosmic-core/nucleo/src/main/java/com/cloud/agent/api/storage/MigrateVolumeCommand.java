//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.storage.StoragePool;
import com.cloud.storage.Volume;

public class MigrateVolumeCommand extends Command {

    long volumeId;
    String volumePath;
    StorageFilerTO pool;
    String attachedVmName;
    Volume.Type volumeType;

    public MigrateVolumeCommand(final long volumeId, final String volumePath, final StoragePool pool, final int timeout) {
        this.volumeId = volumeId;
        this.volumePath = volumePath;
        this.pool = new StorageFilerTO(pool);
        this.setWait(timeout);
    }

    public MigrateVolumeCommand(final long volumeId, final String volumePath, final StoragePool pool, final String attachedVmName, final Volume.Type volumeType, final int
            timeout) {
        this.volumeId = volumeId;
        this.volumePath = volumePath;
        this.pool = new StorageFilerTO(pool);
        this.attachedVmName = attachedVmName;
        this.volumeType = volumeType;
        this.setWait(timeout);
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getVolumePath() {
        return volumePath;
    }

    public long getVolumeId() {
        return volumeId;
    }

    public StorageFilerTO getPool() {
        return pool;
    }

    public String getAttachedVmName() {
        return attachedVmName;
    }

    public Volume.Type getVolumeType() {
        return volumeType;
    }
}
