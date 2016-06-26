//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.VirtualMachineTO;

public class MigrateWithStorageCompleteCommand extends Command {
    VirtualMachineTO vm;

    public MigrateWithStorageCompleteCommand(final VirtualMachineTO vm) {
        this.vm = vm;
    }

    public VirtualMachineTO getVirtualMachine() {
        return vm;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
