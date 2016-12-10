package com.cloud.network.lb;

import com.cloud.network.rules.LoadBalancer;

public interface ApplicationLoadBalancerRule extends ApplicationLoadBalancerContainer, LoadBalancer {
    int getInstancePort();
}
