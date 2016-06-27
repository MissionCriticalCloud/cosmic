package com.cloud.network.rules;

public interface StaticNatRule extends FirewallRule {

    String getDestIpAddress();
}
