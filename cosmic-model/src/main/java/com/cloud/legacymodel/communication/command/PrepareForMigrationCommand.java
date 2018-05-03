package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.VirtualMachineTO;

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
