package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.CreateVMSnapshotCommand;
import com.cloud.legacymodel.to.VMSnapshotTO;
import com.cloud.legacymodel.to.VolumeObjectTO;

import java.util.List;

public class CreateVMSnapshotAnswer extends Answer {

    private List<VolumeObjectTO> volumeTOs;
    private VMSnapshotTO vmSnapshotTo;

    public CreateVMSnapshotAnswer() {

    }

    public CreateVMSnapshotAnswer(final CreateVMSnapshotCommand cmd, final boolean success, final String result) {
        super(cmd, success, result);
    }

    public CreateVMSnapshotAnswer(final CreateVMSnapshotCommand cmd, final VMSnapshotTO vmSnapshotTo, final List<VolumeObjectTO> volumeTOs) {
        super(cmd, true, "");
        this.vmSnapshotTo = vmSnapshotTo;
        this.volumeTOs = volumeTOs;
    }

    public List<VolumeObjectTO> getVolumeTOs() {
        return volumeTOs;
    }

    public void setVolumeTOs(final List<VolumeObjectTO> volumeTOs) {
        this.volumeTOs = volumeTOs;
    }

    public VMSnapshotTO getVmSnapshotTo() {
        return vmSnapshotTo;
    }

    public void setVmSnapshotTo(final VMSnapshotTO vmSnapshotTo) {
        this.vmSnapshotTo = vmSnapshotTo;
    }
}
