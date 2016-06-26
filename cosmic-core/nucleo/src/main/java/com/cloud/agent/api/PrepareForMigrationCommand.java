//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.VirtualMachineTO;

public class PrepareForMigrationCommand extends Command {
    VirtualMachineTO vm;

    protected PrepareForMigrationCommand() {
    }

    public PrepareForMigrationCommand(final VirtualMachineTO vm) {
        this.vm = vm;
    }

    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
