//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.LoadBalancerTO;

/**
 * LoadBalancerConfigCommand sends the load balancer configuration
 */
public class HealthCheckLBConfigCommand extends NetworkElementCommand {
    LoadBalancerTO[] loadBalancers;

    protected HealthCheckLBConfigCommand() {
    }

    public HealthCheckLBConfigCommand(final LoadBalancerTO[] loadBalancers) {
        this.loadBalancers = loadBalancers;
    }

    public LoadBalancerTO[] getLoadBalancers() {
        return loadBalancers;
    }
}
