package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.DeleteVMSnapshotCommand;
import com.cloud.legacymodel.to.VolumeObjectTO;

import java.util.List;

public class DeleteVMSnapshotAnswer extends Answer {
    private List<VolumeObjectTO> volumeTOs;

    public DeleteVMSnapshotAnswer() {
    }

    public DeleteVMSnapshotAnswer(final DeleteVMSnapshotCommand cmd, final boolean result, final String message) {
        super(cmd, result, message);
    }

    public DeleteVMSnapshotAnswer(final DeleteVMSnapshotCommand cmd, final List<VolumeObjectTO> volumeTOs) {
        super(cmd, true, "");
        this.volumeTOs = volumeTOs;
    }

    public List<VolumeObjectTO> getVolumeTOs() {
        return volumeTOs;
    }

    public void setVolumeTOs(final List<VolumeObjectTO> volumeTOs) {
        this.volumeTOs = volumeTOs;
    }
}
