package com.cloud.legacymodel.communication.command;

public class CheckHealthCommand extends Command {

    public CheckHealthCommand() {
        setWait(50);
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
