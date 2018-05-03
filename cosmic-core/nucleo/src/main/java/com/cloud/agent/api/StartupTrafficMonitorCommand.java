package com.cloud.agent.api;

import com.cloud.model.enumeration.HostType;

public class StartupTrafficMonitorCommand extends StartupCommand {
    public StartupTrafficMonitorCommand() {
        super(HostType.TrafficMonitor);
    }
}
