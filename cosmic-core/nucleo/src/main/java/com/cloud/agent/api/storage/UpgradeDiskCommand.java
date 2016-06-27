//

//

package com.cloud.agent.api.storage;

public class UpgradeDiskCommand extends StorageCommand {

    private String _imagePath;
    private String _newSize;

    public UpgradeDiskCommand() {
    }

    public UpgradeDiskCommand(final String imagePath, final String newSize) {
        _imagePath = imagePath;
        _newSize = newSize;
    }

    public String getImagePath() {
        return _imagePath;
    }

    public void setImagePath(final String imagePath) {
        _imagePath = imagePath;
    }

    public String getNewSize() {
        return _newSize;
    }

    public void setNewSize(final String newSize) {
        _newSize = newSize;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
