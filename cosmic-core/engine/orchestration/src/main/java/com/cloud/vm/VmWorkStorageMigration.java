package com.cloud.vm;

public class VmWorkStorageMigration extends VmWork {
    Long destPoolId;

    public VmWorkStorageMigration(final long userId, final long accountId, final long vmId, final String handlerName, final Long destPoolId) {
        super(userId, accountId, vmId, handlerName);

        this.destPoolId = destPoolId;
    }

    public Long getDestStoragePoolId() {
        return destPoolId;
    }
}
