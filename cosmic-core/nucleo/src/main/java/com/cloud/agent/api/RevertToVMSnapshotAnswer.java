//

//

package com.cloud.agent.api;

import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.storage.to.VolumeObjectTO;

import java.util.List;

public class RevertToVMSnapshotAnswer extends Answer {

    private List<VolumeObjectTO> volumeTOs;
    private VirtualMachine.PowerState vmState;

    public RevertToVMSnapshotAnswer(final RevertToVMSnapshotCommand cmd, final boolean result, final String message) {
        super(cmd, result, message);
    }

    public RevertToVMSnapshotAnswer() {
        super();
    }

    public RevertToVMSnapshotAnswer(final RevertToVMSnapshotCommand cmd, final List<VolumeObjectTO> volumeTOs, final VirtualMachine.PowerState vmState) {
        super(cmd, true, "");
        this.volumeTOs = volumeTOs;
        this.vmState = vmState;
    }

    public VirtualMachine.PowerState getVmState() {
        return vmState;
    }

    public void setVmState(final VirtualMachine.PowerState vmState) {
        this.vmState = vmState;
    }

    public List<VolumeObjectTO> getVolumeTOs() {
        return volumeTOs;
    }

    public void setVolumeTOs(final List<VolumeObjectTO> volumeTOs) {
        this.volumeTOs = volumeTOs;
    }
}
