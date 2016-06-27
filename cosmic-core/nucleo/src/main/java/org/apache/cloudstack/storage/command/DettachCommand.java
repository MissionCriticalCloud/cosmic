//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.to.DiskTO;

public class DettachCommand extends StorageSubSystemCommand {
    private DiskTO disk;
    private String vmName;
    private boolean _managed;
    private String _iScsiName;
    private String _storageHost;
    private int _storagePort;

    public DettachCommand(final DiskTO disk, final String vmName) {
        super();
        this.disk = disk;
        this.vmName = vmName;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public DiskTO getDisk() {
        return disk;
    }

    public void setDisk(final DiskTO disk) {
        this.disk = disk;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(final String vmName) {
        this.vmName = vmName;
    }

    public boolean isManaged() {
        return _managed;
    }

    public void setManaged(final boolean managed) {
        _managed = managed;
    }

    public String get_iScsiName() {
        return _iScsiName;
    }

    public void set_iScsiName(final String iScsiName) {
        _iScsiName = iScsiName;
    }

    public String getStorageHost() {
        return _storageHost;
    }

    public void setStorageHost(final String storageHost) {
        _storageHost = storageHost;
    }

    public int getStoragePort() {
        return _storagePort;
    }

    public void setStoragePort(final int storagePort) {
        _storagePort = storagePort;
    }

    @Override
    public void setExecuteInSequence(final boolean inSeq) {

    }
}
