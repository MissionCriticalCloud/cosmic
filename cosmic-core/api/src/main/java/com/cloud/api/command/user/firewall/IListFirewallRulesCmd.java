package com.cloud.api.command.user.firewall;

import com.cloud.api.IBaseListTaggedResourcesCmd;
import com.cloud.legacymodel.network.FirewallRule;

public interface IListFirewallRulesCmd extends IBaseListTaggedResourcesCmd {
    Long getIpAddressId();

    FirewallRule.TrafficType getTrafficType();

    Long getId();

    Long getNetworkId();
}
