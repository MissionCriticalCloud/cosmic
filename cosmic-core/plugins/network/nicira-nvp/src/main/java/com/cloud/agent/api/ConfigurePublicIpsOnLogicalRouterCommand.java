//

//

package com.cloud.agent.api;

import java.util.List;

public class ConfigurePublicIpsOnLogicalRouterCommand extends Command {

    private String logicalRouterUuid;
    private String l3GatewayServiceUuid;
    private List<String> publicCidrs;

    public ConfigurePublicIpsOnLogicalRouterCommand(final String logicalRouterUuid, final String l3GatewayServiceUuid, final List<String> publicCidrs) {
        super();
        this.logicalRouterUuid = logicalRouterUuid;
        this.publicCidrs = publicCidrs;
        this.l3GatewayServiceUuid = l3GatewayServiceUuid;
    }

    public String getLogicalRouterUuid() {
        return logicalRouterUuid;
    }

    public void setLogicalRouterUuid(final String logicalRouterUuid) {
        this.logicalRouterUuid = logicalRouterUuid;
    }

    public String getL3GatewayServiceUuid() {
        return l3GatewayServiceUuid;
    }

    public void setL3GatewayServiceUuid(final String l3GatewayServiceUuid) {
        this.l3GatewayServiceUuid = l3GatewayServiceUuid;
    }

    public List<String> getPublicCidrs() {
        return publicCidrs;
    }

    public void setPublicCidrs(final List<String> publicCidrs) {
        this.publicCidrs = publicCidrs;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
