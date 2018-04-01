package com.cloud.storage.command;

import com.cloud.agent.api.to.DiskTO;

public final class AttachCommand extends StorageSubSystemCommand {
    private DiskTO disk;
    private String vmName;
    private boolean inSeq = false;

    public AttachCommand(final DiskTO disk, final String vmName) {
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

    @Override
    public void setExecuteInSequence(final boolean inSeq) {
        this.inSeq = inSeq;
    }
}
