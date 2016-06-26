package com.cloud.agent.api.storage;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.storage.StoragePool;

public class CreateVolumeOVACommand extends Command {
    String secUrl;
    String volPath;
    String volName;
    StorageFilerTO pool;

    public CreateVolumeOVACommand() {
    }

    public CreateVolumeOVACommand(final String secUrl, final String volPath, final String volName, final StoragePool pool, final int wait) {
        this.secUrl = secUrl;
        this.volPath = volPath;
        this.volName = volName;
        this.pool = new StorageFilerTO(pool);
        setWait(wait);
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getVolPath() {
        return this.volPath;
    }

    public String getVolName() {
        return this.volName;
    }

    public String getSecondaryStorageUrl() {
        return this.secUrl;
    }

    public StorageFilerTO getPool() {
        return pool;
    }
}
