package com.cloud.agent.api;

import com.cloud.legacymodel.communication.command.Command;

public class CheckHealthCommand extends Command {

    public CheckHealthCommand() {
        setWait(50);
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
