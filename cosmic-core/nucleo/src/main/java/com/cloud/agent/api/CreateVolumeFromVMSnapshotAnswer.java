//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.VolumeTO;

public class CreateVolumeFromVMSnapshotAnswer extends Answer {
    private String path;
    private VolumeTO volumeTo;

    public CreateVolumeFromVMSnapshotAnswer(final CreateVolumeFromVMSnapshotCommand cmd, final VolumeTO volumeTo) {
        super(cmd, true, "");
        this.volumeTo = volumeTo;
    }

    protected CreateVolumeFromVMSnapshotAnswer() {

    }

    public CreateVolumeFromVMSnapshotAnswer(final CreateVolumeFromVMSnapshotCommand cmd, final String path) {
        super(cmd, true, "");
        this.path = path;
    }

    public CreateVolumeFromVMSnapshotAnswer(final CreateVolumeFromVMSnapshotCommand cmd, final boolean result, final String string) {
        super(cmd, result, string);
    }

    public VolumeTO getVolumeTo() {
        return volumeTo;
    }

    public String getPath() {
        return path;
    }
}
