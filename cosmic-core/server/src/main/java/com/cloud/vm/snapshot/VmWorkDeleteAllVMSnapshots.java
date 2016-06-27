package com.cloud.vm.snapshot;

import com.cloud.vm.VmWork;

public class VmWorkDeleteAllVMSnapshots extends VmWork {
    private static final long serialVersionUID = -6010083039865471888L;

    private final VMSnapshot.Type type;

    public VmWorkDeleteAllVMSnapshots(final long userId, final long accountId, final long vmId, final String handlerName, final VMSnapshot.Type type) {
        super(userId, accountId, vmId, handlerName);

        this.type = type;
    }

    public VMSnapshot.Type getSnapshotType() {
        return type;
    }
}
