package com.cloud.vm.snapshot;

import com.cloud.vm.VmWork;

public class VmWorkCreateVMSnapshot extends VmWork {
    private static final long serialVersionUID = 124386202146049838L;

    private final Long vmSnapshotId;
    private final boolean quiesceVm;

    public VmWorkCreateVMSnapshot(final long userId, final long accountId, final long vmId, final String handlerName, final Long vmSnapshotId, final boolean quiesceVm) {
        super(userId, accountId, vmId, handlerName);

        this.vmSnapshotId = vmSnapshotId;
        this.quiesceVm = quiesceVm;
    }

    public Long getVmSnapshotId() {
        return vmSnapshotId;
    }

    public boolean isQuiesceVm() {
        return quiesceVm;
    }
}
