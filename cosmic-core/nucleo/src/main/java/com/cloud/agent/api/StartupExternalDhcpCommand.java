package com.cloud.agent.api;

import com.cloud.model.enumeration.HostType;

public class StartupExternalDhcpCommand extends StartupCommand {
    public StartupExternalDhcpCommand() {
        super(HostType.ExternalDhcp);
    }
}
