package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.RestoreVMSnapshotCommand;
import com.cloud.legacymodel.to.VolumeObjectTO;
import com.cloud.legacymodel.vm.VirtualMachine.PowerState;

import java.util.List;

public class RestoreVMSnapshotAnswer extends Answer {

    private List<VolumeObjectTO> volumeTOs;
    private PowerState vmState;

    public RestoreVMSnapshotAnswer(final RestoreVMSnapshotCommand cmd, final boolean result, final String message) {
        super(cmd, result, message);
    }

    public RestoreVMSnapshotAnswer() {
        super();
    }

    public RestoreVMSnapshotAnswer(final RestoreVMSnapshotCommand cmd, final List<VolumeObjectTO> volumeTOs, final PowerState vmState) {
        super(cmd, true, "");
        this.volumeTOs = volumeTOs;
        this.vmState = vmState;
    }

    public PowerState getVmState() {
        return vmState;
    }

    public List<VolumeObjectTO> getVolumeTOs() {
        return volumeTOs;
    }

    public void setVolumeTOs(final List<VolumeObjectTO> volumeTOs) {
        this.volumeTOs = volumeTOs;
    }

    public void setVmState(final PowerState vmState) {
        this.vmState = vmState;
    }
}
