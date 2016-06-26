//

//

package com.cloud.agent.api;

public class CheckVirtualMachineCommand extends Command {

    private String vmName;

    protected CheckVirtualMachineCommand() {

    }

    public CheckVirtualMachineCommand(final String vmName) {
        this.vmName = vmName;
        setWait(20);
    }

    public String getVmName() {
        return vmName;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
