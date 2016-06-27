//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.storage.StoragePool;

public class CreatePrivateTemplateFromVolumeCommand extends SnapshotCommand {
    StorageFilerTO _primaryPool;
    private String _vmName;
    private String _volumePath;
    private String _userSpecifiedName;
    private String _uniqueName;
    private long _templateId;
    private long _accountId;
    // For XenServer
    private String _secondaryStorageUrl;

    public CreatePrivateTemplateFromVolumeCommand() {
    }

    public CreatePrivateTemplateFromVolumeCommand(final StoragePool pool, final String secondaryStorageUrl, final long templateId, final long accountId, final String
            userSpecifiedName,
                                                  final String uniqueName, final String volumePath, final String vmName, final int wait) {
        _secondaryStorageUrl = secondaryStorageUrl;
        _templateId = templateId;
        _accountId = accountId;
        _userSpecifiedName = userSpecifiedName;
        _uniqueName = uniqueName;
        _volumePath = volumePath;
        _vmName = vmName;
        primaryStoragePoolNameLabel = pool.getUuid();
        _primaryPool = new StorageFilerTO(pool);
        setWait(wait);
    }

    @Override
    public StorageFilerTO getPool() {
        return _primaryPool;
    }

    @Override
    public String getSecondaryStorageUrl() {
        return _secondaryStorageUrl;
    }

    @Override
    public Long getAccountId() {
        return _accountId;
    }

    @Override
    public String getVolumePath() {
        return _volumePath;
    }

    @Override
    public void setVolumePath(final String volumePath) {
        this._volumePath = volumePath;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getTemplateName() {
        return _userSpecifiedName;
    }

    public String getUniqueName() {
        return _uniqueName;
    }

    public long getTemplateId() {
        return _templateId;
    }

    public void setTemplateId(final long templateId) {
        _templateId = templateId;
    }

    public String getVmName() {
        return _vmName;
    }
}
