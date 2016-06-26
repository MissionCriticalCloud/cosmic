//

//

package com.cloud.agent.api;

import com.cloud.storage.StoragePool;

public class UpgradeSnapshotCommand extends SnapshotCommand {
    private String version;
    private Long templateId;
    private Long tmpltAccountId;

    protected UpgradeSnapshotCommand() {

    }

    /**
     * @param primaryStoragePoolNameLabel The UUID of the primary storage Pool
     * @param secondaryStoragePoolURL     This is what shows up in the UI when you click on Secondary storage.
     * @param snapshotUuid                The UUID of the snapshot which is going to be upgraded
     * @param _version                    version for this snapshot
     */
    public UpgradeSnapshotCommand(final StoragePool pool, final String secondaryStoragePoolURL, final Long dcId, final Long accountId, final Long volumeId, final Long
            templateId, final Long tmpltAccountId,
                                  final String volumePath, final String snapshotUuid, final String snapshotName, final String version) {
        super(pool, secondaryStoragePoolURL, snapshotUuid, snapshotName, dcId, accountId, volumeId);
        this.version = version;
        this.templateId = templateId;
        this.tmpltAccountId = tmpltAccountId;
    }

    public String getVersion() {
        return version;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public Long getTmpltAccountId() {
        return tmpltAccountId;
    }
}
