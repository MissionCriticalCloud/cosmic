package com.cloud.legacymodel.communication.command;

public class MigrationProgressCommand extends Command {
    String vmName;

    protected MigrationProgressCommand() {
        super();
    }

    public MigrationProgressCommand(final String vmName) {
        this.vmName = vmName;
    }

    public String getVmName() {
        return vmName;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
