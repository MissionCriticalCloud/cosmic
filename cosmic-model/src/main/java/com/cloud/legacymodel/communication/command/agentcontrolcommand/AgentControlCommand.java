package com.cloud.legacymodel.communication.command.agentcontrolcommand;

import com.cloud.legacymodel.communication.command.Command;

public class AgentControlCommand extends Command {

    public AgentControlCommand() {
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
