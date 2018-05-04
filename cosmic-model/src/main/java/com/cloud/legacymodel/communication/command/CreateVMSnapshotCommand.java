package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.VMSnapshotTO;
import com.cloud.legacymodel.to.VolumeObjectTO;

import java.util.List;

public class CreateVMSnapshotCommand extends VMSnapshotBaseCommand {
    private final String vmUuid;

    public CreateVMSnapshotCommand(final String vmName, final String vmUuid, final VMSnapshotTO snapshot, final List<VolumeObjectTO> volumeTOs, final String guestOSType) {
        super(vmName, snapshot, volumeTOs, guestOSType);
        this.vmUuid = vmUuid;
    }

    public String getVmUuid() {
        return vmUuid;
    }
}
