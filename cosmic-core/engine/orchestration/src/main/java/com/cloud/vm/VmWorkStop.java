package com.cloud.vm;

public class VmWorkStop extends VmWork {
    private final boolean cleanup;

    public VmWorkStop(final long userId, final long accountId, final long vmId, final String handlerName, final boolean cleanup) {
        super(userId, accountId, vmId, handlerName);
        this.cleanup = cleanup;
    }

    public boolean isCleanup() {
        return cleanup;
    }
}
