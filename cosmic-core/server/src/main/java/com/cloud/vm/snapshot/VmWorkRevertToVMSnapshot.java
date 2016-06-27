package com.cloud.vm.snapshot;

import com.cloud.vm.VmWork;

public class VmWorkRevertToVMSnapshot extends VmWork {
    private static final long serialVersionUID = -3406543280278986843L;

    private final Long vmSnapshotId;

    public VmWorkRevertToVMSnapshot(final long userId, final long accountId, final long vmId, final String handlerName, final Long vmSnapshotId) {
        super(userId, accountId, vmId, handlerName);

        this.vmSnapshotId = vmSnapshotId;
    }

    public Long getVmSnapshotId() {
        return vmSnapshotId;
    }
}
