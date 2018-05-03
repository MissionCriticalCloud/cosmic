package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.VMSnapshotTO;
import com.cloud.legacymodel.to.VolumeObjectTO;

import java.util.List;
import java.util.Map;

public class RestoreVMSnapshotCommand extends VMSnapshotBaseCommand {

    private List<VMSnapshotTO> snapshots;
    private Map<Long, VMSnapshotTO> snapshotAndParents;

    public RestoreVMSnapshotCommand(final String vmName, final VMSnapshotTO snapshot, final List<VolumeObjectTO> volumeTOs, final String guestOSType) {
        super(vmName, snapshot, volumeTOs, guestOSType);
    }

    public List<VMSnapshotTO> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(final List<VMSnapshotTO> snapshots) {
        this.snapshots = snapshots;
    }

    public Map<Long, VMSnapshotTO> getSnapshotAndParents() {
        return snapshotAndParents;
    }

    public void setSnapshotAndParents(final Map<Long, VMSnapshotTO> snapshotAndParents) {
        this.snapshotAndParents = snapshotAndParents;
    }
}
