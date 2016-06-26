package com.cloud.vm;

public class VmWorkStorageMigration extends VmWork {
    private static final long serialVersionUID = -8677979691741157474L;

    Long destPoolId;

    public VmWorkStorageMigration(final long userId, final long accountId, final long vmId, final String handlerName, final Long destPoolId) {
        super(userId, accountId, vmId, handlerName);

        this.destPoolId = destPoolId;
    }

    public Long getDestStoragePoolId() {
        return destPoolId;
    }
}
