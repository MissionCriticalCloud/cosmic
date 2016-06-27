//

//

package com.cloud.agent.api;

import com.cloud.vm.VirtualMachine;

public class NetworkRulesSystemVmCommand extends Command {

    private String vmName;
    private VirtualMachine.Type type;

    protected NetworkRulesSystemVmCommand() {

    }

    public NetworkRulesSystemVmCommand(final String vmName, final VirtualMachine.Type type) {
        this.vmName = vmName;
        this.type = type;
    }

    public String getVmName() {
        return vmName;
    }

    public VirtualMachine.Type getType() {
        return type;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
