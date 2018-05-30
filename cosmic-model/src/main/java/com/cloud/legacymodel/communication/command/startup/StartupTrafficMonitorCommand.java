package com.cloud.legacymodel.communication.command.startup;

import com.cloud.model.enumeration.HostType;

public class StartupTrafficMonitorCommand extends StartupCommand {
    public StartupTrafficMonitorCommand() {
        super(HostType.TrafficMonitor);
    }
}
