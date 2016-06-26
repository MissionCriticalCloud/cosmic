//

//

package com.cloud.agent.api;

public class ModifyVmNicConfigCommand extends Command {
    String vmName;
    String vlan;
    String macAddress;
    int index;
    boolean enable;
    String switchLableName;

    protected ModifyVmNicConfigCommand() {
    }

    public ModifyVmNicConfigCommand(final String vmName, final String vlan, final String macAddress) {
        this.vmName = vmName;
        this.vlan = vlan;
        this.macAddress = macAddress;
    }

    public ModifyVmNicConfigCommand(final String vmName, final String vlan, final int position) {
        this.vmName = vmName;
        this.vlan = vlan;
        this.index = position;
    }

    public ModifyVmNicConfigCommand(final String vmName, final String vlan, final int position, final boolean enable) {
        this.vmName = vmName;
        this.vlan = vlan;
        this.index = position;
        this.enable = enable;
    }

    public String getVmName() {
        return vmName;
    }

    public String getSwitchLableName() {
        return switchLableName;
    }

    public void setSwitchLableName(final String switchlableName) {
        this.switchLableName = switchlableName;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
