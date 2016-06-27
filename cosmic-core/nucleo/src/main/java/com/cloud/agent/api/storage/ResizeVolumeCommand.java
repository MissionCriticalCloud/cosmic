//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.to.StorageFilerTO;

public class ResizeVolumeCommand extends Command {
    private String path;
    private StorageFilerTO pool;
    private String vmInstance;
    private Long newSize;
    private Long currentSize;
    private boolean shrinkOk;

    protected ResizeVolumeCommand() {

    }

    public ResizeVolumeCommand(final String path, final StorageFilerTO pool, final Long currentSize, final Long newSize, final boolean shrinkOk, final String vmInstance) {
        this.path = path;
        this.pool = pool;
        this.vmInstance = vmInstance;
        this.currentSize = currentSize;
        this.newSize = newSize;
        this.shrinkOk = shrinkOk;
    }

    public String getPath() {
        return path;
    }

    public String getPoolUuid() {
        return pool.getUuid();
    }

    public StorageFilerTO getPool() {
        return pool;
    }

    public long getNewSize() {
        return newSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public boolean getShrinkOk() {
        return shrinkOk;
    }

    public String getInstanceName() {
        return vmInstance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeInSequence() {
        return false;
    }
}
