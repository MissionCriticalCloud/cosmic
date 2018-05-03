package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.to.VolumeObjectTO;

import java.util.List;

public class MigrateWithStorageAcrossClustersAnswer extends Answer {

    private List<VolumeObjectTO> volumes;

    public MigrateWithStorageAcrossClustersAnswer(final MigrateWithStorageAcrossClustersCommand cmd, final List<VolumeObjectTO> volumes) {
        super(cmd);
        this.volumes = volumes;
    }

    public MigrateWithStorageAcrossClustersAnswer(final MigrateWithStorageAcrossClustersCommand cmd, final Exception ex) {
        super(cmd, false, ex.getMessage());
    }

    public List<VolumeObjectTO> getVolumes() {
        return volumes;
    }
}
