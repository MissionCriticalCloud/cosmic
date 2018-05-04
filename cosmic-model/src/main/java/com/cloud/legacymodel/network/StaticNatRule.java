package com.cloud.legacymodel.network;

public interface StaticNatRule extends FirewallRule {

    String getDestIpAddress();
}
