//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.MonitorServiceTO;

import java.util.List;

/**
 * AccessDetails allow different components to put in information about
 * how to access the components inside the command.
 */
public class SetMonitorServiceCommand extends NetworkElementCommand {
    MonitorServiceTO[] services;

    protected SetMonitorServiceCommand() {
    }

    public SetMonitorServiceCommand(final List<MonitorServiceTO> services) {
        this.services = services.toArray(new MonitorServiceTO[services.size()]);
    }

    public MonitorServiceTO[] getRules() {
        return services;
    }

    public String getConfiguration() {

        final StringBuilder sb = new StringBuilder();
        for (final MonitorServiceTO service : services) {
            sb.append("[").append(service.getService()).append("]").append(":");
            sb.append("processname=").append(service.getProcessname()).append(":");
            sb.append("servicename=").append(service.getServiceName()).append(":");
            sb.append("pidfile=").append(service.getPidFile()).append(":");
            sb.append(",");
        }

        return sb.toString();
    }
}
