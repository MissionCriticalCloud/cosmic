package com.cloud.agent.api;

import com.cloud.legacymodel.communication.command.Command;

public class DeleteLogicalSwitchCommand extends Command {

    private final String logicalSwitchUuid;

    public DeleteLogicalSwitchCommand(final String logicalSwitchUuid) {
        this.logicalSwitchUuid = logicalSwitchUuid;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getLogicalSwitchUuid() {
        return logicalSwitchUuid;
    }
}
