//

//

package com.cloud.agent.api;

import com.cloud.vm.VirtualMachine;

public class RebootCommand extends Command {
    String vmName;

    protected RebootCommand() {
    }

    public RebootCommand(final VirtualMachine vm) {
        vmName = vm.getInstanceName();
    }

    public RebootCommand(final String vmName) {
        this.vmName = vmName;
    }

    public String getVmName() {
        return vmName;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
