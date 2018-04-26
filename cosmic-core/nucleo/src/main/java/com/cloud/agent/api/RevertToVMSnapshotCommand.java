package com.cloud.agent.api;

import com.cloud.storage.to.VolumeObjectTO;

import java.util.List;

public class RevertToVMSnapshotCommand extends VMSnapshotBaseCommand {

    private boolean reloadVm = false;
    private final String vmUuid;

    public RevertToVMSnapshotCommand(final String vmName, final String vmUuid, final VMSnapshotTO snapshot, final List<VolumeObjectTO> volumeTOs, final
    boolean reloadVm) {
        this(vmName, vmUuid, snapshot, volumeTOs);
        setReloadVm(reloadVm);
    }

    public RevertToVMSnapshotCommand(final String vmName, final String vmUuid, final VMSnapshotTO snapshot, final List<VolumeObjectTO> volumeTOs) {
        super(vmName, snapshot, volumeTOs);
        this.vmUuid = vmUuid;
    }

    public boolean isReloadVm() {
        return reloadVm;
    }

    public void setReloadVm(final boolean reloadVm) {
        this.reloadVm = reloadVm;
    }

    public String getVmUuid() {
        return vmUuid;
    }
}
