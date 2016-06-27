//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;

public class CopyVolumeAnswer extends Answer {
    private String volumeFolder;
    private String volumePath;

    protected CopyVolumeAnswer() {
        super();
    }

    public CopyVolumeAnswer(final Command command, final boolean success, final String details, final String volumeFolder, final String volumePath) {
        super(command, success, details);
        this.volumeFolder = volumeFolder;
        this.volumePath = volumePath;
    }

    public String getVolumeFolder() {
        return volumeFolder;
    }

    public String getVolumePath() {
        return volumePath;
    }
}
