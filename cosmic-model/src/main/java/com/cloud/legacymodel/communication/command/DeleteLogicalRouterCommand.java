package com.cloud.legacymodel.communication.command;

public class DeleteLogicalRouterCommand extends Command {

    private final String logicalRouterUuid;

    public DeleteLogicalRouterCommand(final String logicalRouterUuid) {
        this.logicalRouterUuid = logicalRouterUuid;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    public String getLogicalRouterUuid() {
        return logicalRouterUuid;
    }
}
