package com.cloud.legacymodel.communication.command;

public class RebootCommand extends Command {
    String vmName;
    protected boolean executeInSequence = false;

    protected RebootCommand() {
    }

    public RebootCommand(final String vmName, final boolean executeInSequence) {
        this.vmName = vmName;
        this.executeInSequence = executeInSequence;
    }

    public String getVmName() {
        return this.vmName;
    }

    @Override
    public boolean executeInSequence() {
        return this.executeInSequence;
    }
}
