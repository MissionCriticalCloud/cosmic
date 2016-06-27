//

//

package com.cloud.agent.api;

import org.apache.cloudstack.storage.to.VolumeObjectTO;

import java.util.List;

public class MigrateWithStorageAnswer extends Answer {

    List<VolumeObjectTO> volumeTos;

    public MigrateWithStorageAnswer(final MigrateWithStorageCommand cmd, final Exception ex) {
        super(cmd, ex);
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
