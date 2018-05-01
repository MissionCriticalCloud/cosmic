package com.cloud.vm.snapshot;

import com.cloud.vm.VmWork;

public class VmWorkDeleteVMSnapshot extends VmWork {
    private final Long vmSnapshotId;

    public VmWorkDeleteVMSnapshot(final long userId, final long accountId, final long vmId, final String handlerName, final Long vmSnapshotId) {
        super(userId, accountId, vmId, handlerName);

        this.vmSnapshotId = vmSnapshotId;
    }

    public Long getVmSnapshotId() {
        return vmSnapshotId;
    }
}
