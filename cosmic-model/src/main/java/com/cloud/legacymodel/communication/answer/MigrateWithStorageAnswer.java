package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.MigrateWithStorageCommand;
import com.cloud.legacymodel.to.VolumeObjectTO;

import java.util.List;

public class MigrateWithStorageAnswer extends Answer {

    List<VolumeObjectTO> volumeTos;

    public MigrateWithStorageAnswer(final MigrateWithStorageCommand cmd, final Exception ex) {
        super(cmd, false, ex.getMessage());
        volumeTos = null;
    }

    public MigrateWithStorageAnswer(final MigrateWithStorageCommand cmd, final List<VolumeObjectTO> volumeTos) {
        super(cmd, true, null);
        this.volumeTos = volumeTos;
    }

    public List<VolumeObjectTO> getVolumeTos() {
        return volumeTos;
    }
}
