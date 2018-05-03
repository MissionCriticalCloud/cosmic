package com.cloud.network.rules;

import com.cloud.legacymodel.network.FirewallRule;

/**
 * Definition for a LoadBalancer
 */
public interface LoadBalancer extends FirewallRule, LoadBalancerContainer {

    int getDefaultPortStart();

    int getDefaultPortEnd();

    int getClientTimeout();

    int getServerTimeout();
}
