package org.apache.cloudstack.storage.command;

import org.apache.cloudstack.storage.to.SnapshotObjectTO;

public final class RevertSnapshotCommand extends StorageSubSystemCommand {
    private SnapshotObjectTO data;
    private boolean _executeInSequence = false;

    public RevertSnapshotCommand(final SnapshotObjectTO data) {
        super();
        this.data = data;
    }

    protected RevertSnapshotCommand() {
        super();
    }

    public SnapshotObjectTO getData() {
        return this.data;
    }

    @Override
    public void setExecuteInSequence(final boolean executeInSequence) {
        _executeInSequence = executeInSequence;
    }

    @Override
    public boolean executeInSequence() {
        return _executeInSequence;
    }
}
