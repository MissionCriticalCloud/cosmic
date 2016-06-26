//

//

package com.cloud.agent.api;

import com.cloud.vm.VirtualMachine;

public class NetworkRulesVmSecondaryIpCommand extends Command {

    private final String vmName;
    private VirtualMachine.Type type;
    private String vmSecIp;
    private String vmMac;
    private String action;

    public NetworkRulesVmSecondaryIpCommand(final String vmName, final VirtualMachine.Type type) {
        this.vmName = vmName;
        this.type = type;
    }

    public NetworkRulesVmSecondaryIpCommand(final String vmName, final String vmMac, final String secondaryIp, final boolean action) {
        this.vmName = vmName;
        this.vmMac = vmMac;
        this.vmSecIp = secondaryIp;
        if (action) {
            this.action = "-A";
        } else {
            this.action = "-D";
        }
    }

    public String getVmName() {
        return vmName;
    }

    public VirtualMachine.Type getType() {
        return type;
    }

    public String getVmSecIp() {
        return vmSecIp;
    }

    public String getVmMac() {
        return vmMac;
    }

    public String getAction() {
        return action;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
