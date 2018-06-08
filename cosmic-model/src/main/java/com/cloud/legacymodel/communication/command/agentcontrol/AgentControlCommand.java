package com.cloud.legacymodel.communication.command.agentcontrol;

import com.cloud.legacymodel.communication.command.Command;

public class AgentControlCommand extends Command {

    public AgentControlCommand() {
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
