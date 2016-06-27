//

//

package org.apache.cloudstack.storage.command;

import com.cloud.agent.api.to.DiskTO;

import java.util.Map;

public final class AttachCommand extends StorageSubSystemCommand {
    private DiskTO disk;
    private String vmName;
    private boolean inSeq = false;
    private Map<String, String> controllerInfo;

    public AttachCommand(final DiskTO disk, final String vmName) {
        super();
        this.disk = disk;
        this.vmName = vmName;
    }

    public AttachCommand(final DiskTO disk, final String vmName, final Map<String, String> controllerInfo) {
        super();
        this.disk = disk;
        this.vmName = vmName;
        this.controllerInfo = controllerInfo;
    }

    public Map<String, String> getControllerInfo() {
        return controllerInfo;
    }

    public void setControllerInfo(final Map<String, String> controllerInfo) {
        this.controllerInfo = controllerInfo;
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

    @Override
    public void setExecuteInSequence(final boolean inSeq) {
        this.inSeq = inSeq;
    }
}
