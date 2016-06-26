//

//

package com.cloud.agent.api;

public class ValidateSnapshotAnswer extends Answer {
    private String expectedSnapshotBackupUuid;
    private String actualSnapshotBackupUuid;
    private String actualSnapshotUuid;

    protected ValidateSnapshotAnswer() {

    }

    public ValidateSnapshotAnswer(final ValidateSnapshotCommand cmd, final boolean success, final String result, final String expectedSnapshotBackupUuid, final String
            actualSnapshotBackupUuid,
                                  final String actualSnapshotUuid) {
        super(cmd, success, result);
        this.expectedSnapshotBackupUuid = expectedSnapshotBackupUuid;
        this.actualSnapshotBackupUuid = actualSnapshotBackupUuid;
        this.actualSnapshotUuid = actualSnapshotUuid;
    }

    /**
     * @return the expectedSnapshotBackupUuid
     */
    public String getExpectedSnapshotBackupUuid() {
        return expectedSnapshotBackupUuid;
    }

    /**
     * @return the actualSnapshotBackupUuid
     */
    public String getActualSnapshotBackupUuid() {
        return actualSnapshotBackupUuid;
    }

    public String getActualSnapshotUuid() {
        return actualSnapshotUuid;
    }
}
