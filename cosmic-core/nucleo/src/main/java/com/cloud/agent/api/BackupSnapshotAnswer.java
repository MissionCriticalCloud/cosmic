//

//

package com.cloud.agent.api;

public class BackupSnapshotAnswer extends Answer {
    private String backupSnapshotName;
    private boolean full;

    protected BackupSnapshotAnswer() {

    }

    public BackupSnapshotAnswer(final BackupSnapshotCommand cmd, final boolean success, final String result, final String backupSnapshotName, final boolean full) {
        super(cmd, success, result);
        this.backupSnapshotName = backupSnapshotName;
        this.full = full;
    }

    /**
     * @return the backupSnapshotName
     */
    public String getBackupSnapshotName() {
        return backupSnapshotName;
    }

    public boolean isFull() {
        return full;
    }
}
