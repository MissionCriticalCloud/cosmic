package com.cloud.agent.api;

import com.cloud.storage.to.VolumeObjectTO;

import java.util.List;

public class CreateVMSnapshotCommand extends VMSnapshotBaseCommand {
    private final String vmUuid;

    public CreateVMSnapshotCommand(final String vmName, final String vmUuid, final VMSnapshotTO snapshot, final List<VolumeObjectTO> volumeTOs) {
        super(vmName, snapshot, volumeTOs);
        this.vmUuid = vmUuid;
    }

    public String getVmUuid() {
        return vmUuid;
    }
}
