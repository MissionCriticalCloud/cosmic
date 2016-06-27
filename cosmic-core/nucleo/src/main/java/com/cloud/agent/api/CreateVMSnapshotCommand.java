//

//

package com.cloud.agent.api;

import org.apache.cloudstack.storage.to.VolumeObjectTO;

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
