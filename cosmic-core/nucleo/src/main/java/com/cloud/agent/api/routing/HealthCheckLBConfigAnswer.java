//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.to.LoadBalancerTO;

import java.util.List;

/**
 * LoadBalancerConfigCommand sends the load balancer configuration
 */
public class HealthCheckLBConfigAnswer extends Answer {
    List<LoadBalancerTO> loadBalancers;

    protected HealthCheckLBConfigAnswer() {
    }

    public HealthCheckLBConfigAnswer(final List<LoadBalancerTO> loadBalancers) {
        this.loadBalancers = loadBalancers;
    }

    public List<LoadBalancerTO> getLoadBalancers() {
        return loadBalancers;
    }
}
