package com.cloud.vm;

public class VmWorkMigrateAway extends VmWork {

    private static final long serialVersionUID = -5917512239025814373L;

    private final long srcHostId;

    public VmWorkMigrateAway(final long userId, final long accountId, final long vmId, final String handlerName,
                             final long srcHostId) {
        super(userId, accountId, vmId, handlerName);

        this.srcHostId = srcHostId;
    }

    public long getSrcHostId() {
        return srcHostId;
    }
}
