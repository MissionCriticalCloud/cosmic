//

//

package com.cloud.agent.api;

import com.cloud.host.Host;

public class StartupTrafficMonitorCommand extends StartupCommand {
    public StartupTrafficMonitorCommand() {
        super(Host.Type.TrafficMonitor);
    }
}
