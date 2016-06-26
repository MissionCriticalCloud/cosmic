//

//

package com.cloud.agent.api;

import java.util.List;

public class AttachOrDettachConfigDriveCommand extends Command {

    String vmName;
    List<String[]> vmData;
    String configDriveLabel;
    boolean isAttach = false;

    public AttachOrDettachConfigDriveCommand(final String vmName, final List<String[]> vmData, final String label, final boolean attach) {
        this.vmName = vmName;
        this.vmData = vmData;
        this.configDriveLabel = label;
        this.isAttach = attach;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getVmName() {
        return vmName;
    }

    public List<String[]> getVmData() {
        return vmData;
    }

    public boolean isAttach() {
        return isAttach;
    }

    public String getConfigDriveLabel() {
        return configDriveLabel;
    }
}
