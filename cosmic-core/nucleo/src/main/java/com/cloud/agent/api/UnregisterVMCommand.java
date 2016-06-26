//

//

package com.cloud.agent.api;

public class UnregisterVMCommand extends Command {
    String vmName;
    boolean cleanupVmFiles = false;

    public UnregisterVMCommand(final String vmName) {
        this.vmName = vmName;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getVmName() {
        return vmName;
    }

    public boolean getCleanupVmFiles() {
        return this.cleanupVmFiles;
    }

    public void setCleanupVmFiles(final boolean cleanupVmFiles) {
        this.cleanupVmFiles = cleanupVmFiles;
    }
}
