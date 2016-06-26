//

//

/**
 * NetworkElementCommand to destroy a VPX instance on the Netscaler SDX load balancer appliance
 */
//TODO: fill in the Nitro API parameters required
package com.cloud.agent.api.routing;

public class DestroyLoadBalancerApplianceCommand extends NetworkElementCommand {

    String ip;

    public DestroyLoadBalancerApplianceCommand(final String ip) {
        this.ip = ip;
    }

    public String getLoadBalancerIP() {
        return ip;
    }
}
