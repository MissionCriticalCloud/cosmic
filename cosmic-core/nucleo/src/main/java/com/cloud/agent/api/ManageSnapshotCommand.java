//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.storage.StoragePool;

public class ManageSnapshotCommand extends Command {
    // XXX: Should be an enum
    // XXX: Anyway there is something called inheritance in Java
    public static final String CREATE_SNAPSHOT = "-c";
    public static final String DESTROY_SNAPSHOT = "-d";
    StorageFilerTO _pool;
    private String _commandSwitch;
    // Information about the volume that the snapshot is based on
    private String _volumePath = null;
    // Information about the snapshot
    private String _snapshotPath = null;
    private String _snapshotName = null;
    private long _snapshotId;
    private String _vmName = null;

    public ManageSnapshotCommand() {
    }

    public ManageSnapshotCommand(final long snapshotId, final String volumePath, final StoragePool pool, final String preSnapshotPath, final String snapshotName, final String
            vmName) {
        _commandSwitch = ManageSnapshotCommand.CREATE_SNAPSHOT;
        _volumePath = volumePath;
        _pool = new StorageFilerTO(pool);
        _snapshotPath = preSnapshotPath;
        _snapshotName = snapshotName;
        _snapshotId = snapshotId;
        _vmName = vmName;
    }

    public ManageSnapshotCommand(final long snapshotId, final String snapshotPath) {
        _commandSwitch = ManageSnapshotCommand.DESTROY_SNAPSHOT;
        _snapshotPath = snapshotPath;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getCommandSwitch() {
        return _commandSwitch;
    }

    public String getVolumePath() {
        return _volumePath;
    }

    public StorageFilerTO getPool() {
        return _pool;
    }

    public String getSnapshotPath() {
        return _snapshotPath;
    }

    public String getSnapshotName() {
        return _snapshotName;
    }

    public long getSnapshotId() {
        return _snapshotId;
    }

    public String getVmName() {
        return _vmName;
    }
}
