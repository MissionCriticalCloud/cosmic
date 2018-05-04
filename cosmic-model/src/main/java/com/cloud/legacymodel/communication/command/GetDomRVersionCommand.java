package com.cloud.legacymodel.communication.command;

public class GetDomRVersionCommand extends NetworkElementCommand {
    public GetDomRVersionCommand() {
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
