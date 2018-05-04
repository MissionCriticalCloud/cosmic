package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.VirtualMachineTO;

public class MigrateWithStorageCompleteCommand extends Command {

    private VirtualMachineTO vm;

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
