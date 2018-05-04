package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.VMSnapshotTO;
import com.cloud.legacymodel.to.VolumeObjectTO;

import java.util.List;

public class RevertToVMSnapshotCommand extends VMSnapshotBaseCommand {

    private boolean reloadVm = false;
    private final String vmUuid;

    public RevertToVMSnapshotCommand(final String vmName, final String vmUuid, final VMSnapshotTO snapshot, final List<VolumeObjectTO> volumeTOs, final String guestOSType, final
    boolean reloadVm) {
        this(vmName, vmUuid, snapshot, volumeTOs, guestOSType);
        setReloadVm(reloadVm);
    }

    public RevertToVMSnapshotCommand(final String vmName, final String vmUuid, final VMSnapshotTO snapshot, final List<VolumeObjectTO> volumeTOs, final String guestOSType) {
        super(vmName, snapshot, volumeTOs, guestOSType);
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
