package com.cloud.legacymodel.communication.command;

public class MaintainCommand extends Command {

    public MaintainCommand() {
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
