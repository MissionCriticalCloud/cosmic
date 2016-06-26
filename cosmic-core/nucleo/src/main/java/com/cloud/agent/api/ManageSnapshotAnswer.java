//

//

package com.cloud.agent.api;

public class ManageSnapshotAnswer extends Answer {
    // For create Snapshot
    private String _snapshotPath;

    public ManageSnapshotAnswer() {
    }

    public ManageSnapshotAnswer(final Command cmd, final boolean success, final String result) {
        super(cmd, success, result);
    }

    // For XenServer
    public ManageSnapshotAnswer(final ManageSnapshotCommand cmd, final long snapshotId, final String snapshotPath, final boolean success, final String result) {
        super(cmd, success, result);
        _snapshotPath = snapshotPath;
    }

    public String getSnapshotPath() {
        return _snapshotPath;
    }
}
