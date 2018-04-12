package com.cloud.agent.api.to;

import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.PortForwardingRule;

/**
 * PortForwardingRuleTO specifies one port forwarding rule.
 */
public class PortForwardingRuleTO extends FirewallRuleTO {
    String dstIp;
    int dstPort;

    protected PortForwardingRuleTO() {
        super();
    }

    public PortForwardingRuleTO(final PortForwardingRule rule, final String srcVlanTag, final String srcIp) {
        super(rule, srcVlanTag, srcIp);
        this.dstIp = rule.getDestinationIpAddress().addr();
        this.dstPort = rule.getDestinationPortStart();
    }

    public PortForwardingRuleTO(final long id, final String srcIp, final int srcPort, final String dstIp, final int dstPort, final String protocol, final boolean revoked, final boolean
            alreadyAdded) {
        super(id, null, srcIp, protocol, srcPort, revoked, alreadyAdded, FirewallRule.Purpose.PortForwarding, null, 0, 0);
        this.dstIp = dstIp;
        this.dstPort = dstPort;
    }

    public String getDstIp() {
        return dstIp;
    }

    public int getDstPort() {
        return dstPort;
    }
}
