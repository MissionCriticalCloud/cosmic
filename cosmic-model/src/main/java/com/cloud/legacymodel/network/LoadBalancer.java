package com.cloud.legacymodel.network;

public interface LoadBalancer extends FirewallRule, LoadBalancerContainer {

    int getDefaultPortStart();

    int getDefaultPortEnd();

    int getClientTimeout();

    int getServerTimeout();
}
