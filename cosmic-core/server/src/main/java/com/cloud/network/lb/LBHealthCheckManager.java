package com.cloud.network.lb;

import com.cloud.network.rules.LoadBalancerContainer.Scheme;

public interface LBHealthCheckManager {

    void updateLBHealthCheck(Scheme scheme);
}
