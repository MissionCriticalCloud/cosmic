package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.to.VolumeObjectTO;

import java.util.List;

public class MigrateWithStorageCompleteAnswer extends Answer {
    List<VolumeObjectTO> volumeTos;

    public MigrateWithStorageCompleteAnswer(final MigrateWithStorageCompleteCommand cmd, final Exception ex) {
        super(cmd, false, ex.getMessage());
        volumeTos = null;
    }

    public MigrateWithStorageCompleteAnswer(final MigrateWithStorageCompleteCommand cmd, final List<VolumeObjectTO> volumeTos) {
        super(cmd, true, null);
        this.volumeTos = volumeTos;
    }

    public List<VolumeObjectTO> getVolumeTos() {
        return volumeTos;
    }
}
