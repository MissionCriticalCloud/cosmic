package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.MigrateWithStorageCompleteCommand;
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
