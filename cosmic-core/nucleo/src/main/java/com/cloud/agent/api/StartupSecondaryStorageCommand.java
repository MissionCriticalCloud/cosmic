package com.cloud.agent.api;

import com.cloud.model.enumeration.HostType;

public class StartupSecondaryStorageCommand extends StartupCommand {

    public StartupSecondaryStorageCommand() {
        super(HostType.SecondaryStorage);
        setIqn("NoIqn");
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
