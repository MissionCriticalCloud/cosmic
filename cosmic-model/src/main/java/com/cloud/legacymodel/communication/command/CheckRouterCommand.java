package com.cloud.legacymodel.communication.command;

public class CheckRouterCommand extends NetworkElementCommand {
    public CheckRouterCommand() {
        super();
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    @Override
    public boolean isQuery() {
        return true;
    }
}
