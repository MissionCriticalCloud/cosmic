package com.cloud.network.rules;

import com.cloud.legacymodel.network.FirewallRule;

public interface StaticNatRule extends FirewallRule {

    String getDestIpAddress();
}
