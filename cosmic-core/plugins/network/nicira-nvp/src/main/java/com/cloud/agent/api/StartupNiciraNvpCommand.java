package com.cloud.agent.api;

import com.cloud.legacymodel.communication.command.StartupCommand;
import com.cloud.model.enumeration.HostType;

public class StartupNiciraNvpCommand extends StartupCommand {

    public StartupNiciraNvpCommand() {
        super(HostType.L2Networking);
    }
}
