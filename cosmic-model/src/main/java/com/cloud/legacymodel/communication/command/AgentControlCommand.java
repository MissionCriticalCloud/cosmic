package com.cloud.legacymodel.communication.command;

public class AgentControlCommand extends Command {

    public AgentControlCommand() {
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
