package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.RevertToVMSnapshotCommand;
import com.cloud.legacymodel.to.VolumeObjectTO;
import com.cloud.legacymodel.vm.VirtualMachine;

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
