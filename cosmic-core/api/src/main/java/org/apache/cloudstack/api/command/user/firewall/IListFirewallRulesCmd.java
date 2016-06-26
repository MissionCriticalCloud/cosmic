package org.apache.cloudstack.api.command.user.firewall;

import com.cloud.network.rules.FirewallRule;
import org.apache.cloudstack.api.IBaseListTaggedResourcesCmd;

public interface IListFirewallRulesCmd extends IBaseListTaggedResourcesCmd {
    Long getIpAddressId();

    FirewallRule.TrafficType getTrafficType();

    Long getId();

    Long getNetworkId();
}
