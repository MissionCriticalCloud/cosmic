package com.cloud.legacymodel.communication.command;

public class FindLogicalSwitchCommand extends Command {
    private final String logicalSwitchUuid;

    public FindLogicalSwitchCommand(final String logicalSwitchUuid) {
        this.logicalSwitchUuid = logicalSwitchUuid;
    }

    public String getLogicalSwitchUuid() {
        return logicalSwitchUuid;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
