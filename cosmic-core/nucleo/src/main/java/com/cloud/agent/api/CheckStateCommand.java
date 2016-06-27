//

//

package com.cloud.agent.api;

/**
 *
 *
 */
public class CheckStateCommand extends Command {
    String vmName;

    public CheckStateCommand() {
    }

    public CheckStateCommand(final String vmName) {
        this.vmName = vmName;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getVmName() {
        return vmName;
    }
}
