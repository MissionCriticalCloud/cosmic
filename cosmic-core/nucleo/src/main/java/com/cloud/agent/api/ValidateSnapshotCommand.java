//

//

package com.cloud.agent.api;

public class ValidateSnapshotCommand extends Command {
    private String primaryStoragePoolNameLabel;
    private String volumeUuid;
    private String firstBackupUuid;
    private String previousSnapshotUuid;
    private String templateUuid;

    protected ValidateSnapshotCommand() {

    }

    /**
     * @param primaryStoragePoolNameLabel The primary storage Pool Name Label
     * @param volumeUuid                  The UUID of the volume for which the snapshot was taken
     * @param firstBackupUuid             This UUID of the first snapshot that was ever taken for this volume, even it was deleted.
     * @param previousSnapshotUuid        The UUID of the previous snapshot on the primary.
     * @param templateUuid                If this is a root volume and no snapshot has been taken for it,
     *                                    this is the UUID of the template VDI.
     */
    public ValidateSnapshotCommand(final String primaryStoragePoolNameLabel, final String volumeUuid, final String firstBackupUuid, final String previousSnapshotUuid, final
    String templateUuid) {
        this.primaryStoragePoolNameLabel = primaryStoragePoolNameLabel;
        this.volumeUuid = volumeUuid;
        this.firstBackupUuid = firstBackupUuid;
        this.previousSnapshotUuid = previousSnapshotUuid;
        this.templateUuid = templateUuid;
    }

    public String getPrimaryStoragePoolNameLabel() {
        return primaryStoragePoolNameLabel;
    }

    /**
     * @return the volumeUuid
     */
    public String getVolumeUuid() {
        return volumeUuid;
    }

    /**
     * @return the firstBackupUuid
     */
    public String getFirstBackupUuid() {
        return firstBackupUuid;
    }

    public String getPreviousSnapshotUuid() {
        return previousSnapshotUuid;
    }

    /**
     * @return the templateUuid
     */
    public String getTemplateUuid() {
        return templateUuid;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
