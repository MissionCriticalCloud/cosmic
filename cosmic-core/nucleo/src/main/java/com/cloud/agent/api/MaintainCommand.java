package com.cloud.agent.api;

import com.cloud.legacymodel.communication.command.Command;

public class MaintainCommand extends Command {

    public MaintainCommand() {
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
