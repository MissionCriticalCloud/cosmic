//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Command;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.storage.StoragePool;
import com.cloud.vm.DiskProfile;

public class CreateCommand extends Command {
    boolean executeInSequence = false;
    private long volId;
    private StorageFilerTO pool;
    private DiskProfile diskCharacteristics;
    private String templateUrl;

    protected CreateCommand() {
        super();
    }

    public CreateCommand(final DiskProfile diskCharacteristics, final String templateUrl, final StoragePool pool, final boolean executeInSequence) {
        this(diskCharacteristics, templateUrl, new StorageFilerTO(pool), executeInSequence);
    }

    /**
     * Construction for template based volumes.
     *
     * @param diskCharacteristics
     * @param templateUrl
     * @param pool
     * @param executeInSequence   TODO
     * @param vol
     * @param vm
     */
    public CreateCommand(final DiskProfile diskCharacteristics, final String templateUrl, final StorageFilerTO pool, final boolean executeInSequence) {
        this(diskCharacteristics, pool, executeInSequence);
        this.templateUrl = templateUrl;
        this.executeInSequence = executeInSequence;
    }

    /**
     * Construction for regular volumes.
     *
     * @param diskCharacteristics
     * @param pool
     * @param executeInSequence   TODO
     * @param vol
     * @param vm
     */
    public CreateCommand(final DiskProfile diskCharacteristics, final StorageFilerTO pool, final boolean executeInSequence) {
        this.volId = diskCharacteristics.getVolumeId();
        this.diskCharacteristics = diskCharacteristics;
        this.pool = pool;
        this.templateUrl = null;
        this.executeInSequence = executeInSequence;
    }

    public CreateCommand(final DiskProfile diskCharacteristics, final StoragePool pool, final boolean executeInSequence) {
        this(diskCharacteristics, new StorageFilerTO(pool), executeInSequence);
        this.executeInSequence = executeInSequence;
    }

    @Override
    public boolean executeInSequence() {
        return executeInSequence;
    }

    public String getTemplateUrl() {
        return templateUrl;
    }

    public StorageFilerTO getPool() {
        return pool;
    }

    public DiskProfile getDiskCharacteristics() {
        return diskCharacteristics;
    }

    public long getVolumeId() {
        return volId;
    }

    @Deprecated
    public String getInstanceName() {
        return null;
    }
}
