//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.storage.StoragePool;

public class CopyVolumeCommand extends Command {

    long volumeId;
    String volumePath;
    StorageFilerTO pool;
    String secondaryStorageURL;
    boolean toSecondaryStorage;
    String vmName;
    boolean executeInSequence = false;

    public CopyVolumeCommand() {
    }

    public CopyVolumeCommand(final long volumeId, final String volumePath, final StoragePool pool, final String secondaryStorageURL, final boolean toSecondaryStorage, final int
            wait,
                             final boolean executeInSequence) {
        this.volumeId = volumeId;
        this.volumePath = volumePath;
        this.pool = new StorageFilerTO(pool);
        this.secondaryStorageURL = secondaryStorageURL;
        this.toSecondaryStorage = toSecondaryStorage;
        setWait(wait);
        this.executeInSequence = executeInSequence;
    }

    @Override
    public boolean executeInSequence() {
        return executeInSequence;
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

    public String getSecondaryStorageURL() {
        return secondaryStorageURL;
    }

    public boolean toSecondaryStorage() {
        return toSecondaryStorage;
    }

    public String getVmName() {
        return vmName;
    }
}
