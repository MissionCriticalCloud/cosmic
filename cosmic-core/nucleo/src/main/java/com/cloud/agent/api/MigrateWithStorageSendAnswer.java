package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.to.VolumeTO;

import java.util.Set;

public class MigrateWithStorageSendAnswer extends Answer {

    private Set<VolumeTO> volumeToSet;

    public MigrateWithStorageSendAnswer(final MigrateWithStorageSendCommand cmd, final Exception ex) {
        super(cmd, false, ex.getMessage());
        volumeToSet = null;
    }

    public MigrateWithStorageSendAnswer(final MigrateWithStorageSendCommand cmd, final Set<VolumeTO> volumeToSet) {
        super(cmd, true, null);
        this.volumeToSet = volumeToSet;
    }

    public Set<VolumeTO> getVolumeToSet() {
        return volumeToSet;
    }
}
