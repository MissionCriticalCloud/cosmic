//

//

package com.cloud.agent.api.routing;

/**
 * NetworkElementCommand to spin a load balancer appliance
 */

public class CreateLoadBalancerApplianceCommand extends NetworkElementCommand {

    String ip;
    String netmask;
    String gateway;
    String username;
    String password;
    String publicInterface;
    String privateInterface;

    public CreateLoadBalancerApplianceCommand(final String ip, final String netmask, final String gateway) {
        this.ip = ip;
        this.netmask = netmask;
        this.gateway = gateway;
    }

    public String getLoadBalancerIP() {
        return ip;
    }

    public String getNetmask() {
        return netmask;
    }

    public String getGateway() {
        return gateway;
    }
}
