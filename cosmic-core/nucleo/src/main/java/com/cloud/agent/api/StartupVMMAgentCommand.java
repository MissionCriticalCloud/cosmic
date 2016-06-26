//

//

package com.cloud.agent.api;

import com.cloud.host.Host;

/**
 * Implementation of bootstrap command sent from management server to agent running on
 * System Center Virtual Machine Manager host
 **/

public class StartupVMMAgentCommand extends Command {
    Host.Type type;
    long dataCenter;
    Long pod;
    String clusterName;
    String guid;
    String managementServerIP;
    String port;
    String version;

    public StartupVMMAgentCommand() {

    }

    public StartupVMMAgentCommand(final long dataCenter, final Long pod, final String clusterName, final String guid, final String managementServerIP, final String port, final
    String version) {
        super();
        this.dataCenter = dataCenter;
        this.pod = pod;
        this.clusterName = clusterName;
        this.guid = guid;
        this.type = Host.Type.Routing;
        this.managementServerIP = managementServerIP;
        this.port = port;
    }

    public long getDataCenter() {
        return dataCenter;
    }

    public Long getPod() {
        return pod;
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getGuid() {
        return guid;
    }

    public String getManagementServerIP() {
        return managementServerIP;
    }

    public String getport() {
        return port;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
