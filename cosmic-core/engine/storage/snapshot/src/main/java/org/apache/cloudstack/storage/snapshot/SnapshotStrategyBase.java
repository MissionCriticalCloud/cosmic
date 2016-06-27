package org.apache.cloudstack.storage.snapshot;

import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotService;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotStrategy;

import javax.inject.Inject;

public abstract class SnapshotStrategyBase implements SnapshotStrategy {
    @Inject
    SnapshotService snapshotSvr;

    @Override
    public SnapshotInfo takeSnapshot(final SnapshotInfo snapshot) {
        return snapshotSvr.takeSnapshot(snapshot).getSnashot();
    }

    @Override
    public SnapshotInfo backupSnapshot(final SnapshotInfo snapshot) {
        return snapshotSvr.backupSnapshot(snapshot);
    }

    @Override
    public boolean revertSnapshot(final SnapshotInfo snapshot) {
        return snapshotSvr.revertSnapshot(snapshot);
    }
}
