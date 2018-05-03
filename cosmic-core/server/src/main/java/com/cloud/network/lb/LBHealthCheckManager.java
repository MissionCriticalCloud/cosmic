package com.cloud.network.lb;

import com.cloud.legacymodel.network.LoadBalancerContainer.Scheme;

public interface LBHealthCheckManager {

    void updateLBHealthCheck(Scheme scheme);
}
