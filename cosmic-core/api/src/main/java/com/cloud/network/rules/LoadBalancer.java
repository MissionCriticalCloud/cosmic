package com.cloud.network.rules;

/**
 * Definition for a LoadBalancer
 */
public interface LoadBalancer extends FirewallRule, LoadBalancerContainer {

    int getDefaultPortStart();

    int getDefaultPortEnd();
}
