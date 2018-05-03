package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.CreateVolumeFromSnapshotCommand;

public class CreateVolumeFromSnapshotAnswer extends Answer {
    private String vdiUUID;

    protected CreateVolumeFromSnapshotAnswer() {

    }

    public CreateVolumeFromSnapshotAnswer(final CreateVolumeFromSnapshotCommand cmd, final boolean success, final String result, final String vdiUUID) {
        super(cmd, success, result);
        this.vdiUUID = vdiUUID;
    }

    /**
     * @return the vdi
     */
    public String getVdi() {
        return vdiUUID;
    }
}
