package com.cloud.agent.api;

import com.cloud.legacymodel.to.VolumeObjectTO;

import java.util.List;

public class DeleteVMSnapshotCommand extends VMSnapshotBaseCommand {
    public DeleteVMSnapshotCommand(final String vmName, final VMSnapshotTO snapshot, final List<VolumeObjectTO> volumeTOs, final String guestOSType) {
        super(vmName, snapshot, volumeTOs, guestOSType);
    }
}
