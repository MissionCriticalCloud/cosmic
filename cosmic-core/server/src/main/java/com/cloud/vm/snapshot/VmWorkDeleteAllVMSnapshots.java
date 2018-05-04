package com.cloud.vm.snapshot;

import com.cloud.legacymodel.storage.VMSnapshot;
import com.cloud.vm.VmWork;

public class VmWorkDeleteAllVMSnapshots extends VmWork {
    private final VMSnapshot.Type type;

    public VmWorkDeleteAllVMSnapshots(final long userId, final long accountId, final long vmId, final String handlerName, final VMSnapshot.Type type) {
        super(userId, accountId, vmId, handlerName);

        this.type = type;
    }

    public VMSnapshot.Type getSnapshotType() {
        return type;
    }
}
