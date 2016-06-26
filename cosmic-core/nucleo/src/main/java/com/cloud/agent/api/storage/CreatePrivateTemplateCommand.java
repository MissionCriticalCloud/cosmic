//

//

package com.cloud.agent.api.storage;

public class CreatePrivateTemplateCommand extends StorageCommand {
    private String _snapshotFolder;
    private String _snapshotPath;
    private String _userFolder;
    private String _userSpecifiedName;
    private String _uniqueName;
    private long _templateId;
    private long _accountId;

    // For XenServer
    private String _secondaryStorageURL;
    private String _snapshotName;

    public CreatePrivateTemplateCommand() {
    }

    public CreatePrivateTemplateCommand(final String secondaryStorageURL, final long templateId, final long accountId, final String userSpecifiedName, final String uniqueName,
                                        final String snapshotFolder,
                                        final String snapshotPath, final String snapshotName, final String userFolder) {
        _secondaryStorageURL = secondaryStorageURL;
        _templateId = templateId;
        _accountId = accountId;
        _userSpecifiedName = userSpecifiedName;
        _uniqueName = uniqueName;
        _snapshotFolder = snapshotFolder;
        _snapshotPath = snapshotPath;
        _snapshotName = snapshotName;
        _userFolder = userFolder;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getSecondaryStorageURL() {
        return _secondaryStorageURL;
    }

    public String getTemplateName() {
        return _userSpecifiedName;
    }

    public String getUniqueName() {
        return _uniqueName;
    }

    public String getSnapshotFolder() {
        return _snapshotFolder;
    }

    public String getSnapshotPath() {
        return _snapshotPath;
    }

    public String getSnapshotName() {
        return _snapshotName;
    }

    public String getUserFolder() {
        return _userFolder;
    }

    public long getTemplateId() {
        return _templateId;
    }

    public void setTemplateId(final long templateId) {
        _templateId = templateId;
    }

    public long getAccountId() {
        return _accountId;
    }
}
