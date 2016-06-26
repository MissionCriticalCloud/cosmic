package com.cloud.vm;

public class VmWorkTakeVolumeSnapshot extends VmWork {

    private static final long serialVersionUID = 341816293003023823L;

    private final Long volumeId;
    private final Long policyId;
    private final Long snapshotId;
    private final boolean quiesceVm;

    public VmWorkTakeVolumeSnapshot(final long userId, final long accountId, final long vmId, final String handlerName,
                                    final Long volumeId, final Long policyId, final Long snapshotId, final boolean quiesceVm) {
        super(userId, accountId, vmId, handlerName);
        this.volumeId = volumeId;
        this.policyId = policyId;
        this.snapshotId = snapshotId;
        this.quiesceVm = quiesceVm;
    }

    public Long getVolumeId() {
        return volumeId;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public Long getSnapshotId() {
        return snapshotId;
    }

    public boolean isQuiesceVm() {
        return quiesceVm;
    }
}
