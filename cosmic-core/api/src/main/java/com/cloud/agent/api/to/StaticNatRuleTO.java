package com.cloud.agent.api.to;

import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.State;
import com.cloud.network.rules.StaticNatRule;

/**
 * StaticNatRuleTO specifies one static nat rule.
 */

public class StaticNatRuleTO extends PortForwardingRuleTO {
    String dstIp;

    protected StaticNatRuleTO() {
    }

    public StaticNatRuleTO(final StaticNatRule rule, final String srcVlanTag, final String srcIp, final String dstIp) {
        super(rule.getId(),
                srcVlanTag,
                srcIp,
                rule.getProtocol(),
                0,
                rule.getState() == State.Revoke,
                rule.getState() == State.Active,
                rule.getPurpose(),
                null,
                0,
                0);
        this.dstIp = dstIp;
    }

    public StaticNatRuleTO(final StaticNatRule rule, final String scrIp, final String dstIp) {
        super(rule.getId(),
                scrIp,
                rule.getProtocol(),
                0,
                rule.getState() == State.Revoke,
                rule.getState() == State.Active,
                rule.getPurpose(),
                null,
                0,
                0);
        this.dstIp = dstIp;
    }

    public StaticNatRuleTO(final long id, final String srcIp, final String dstIp, final String protocol, final
    boolean revoked, final boolean alreadyAdded) {
        super(id, srcIp, protocol, 0, revoked, alreadyAdded, FirewallRule.Purpose.StaticNat, null, 0, 0);
        this.dstIp = dstIp;
    }

    public StaticNatRuleTO(final long id, final String srcVlanTag, final String srcIp, final String dstIp,
                           final String protocol, final boolean revoked, final boolean alreadyAdded) {
        super(id, srcVlanTag, srcIp, protocol, 0, revoked, alreadyAdded, FirewallRule.Purpose.StaticNat, null, 0, 0);
        this.dstIp = dstIp;
    }

    public String getDstIp() {
        return dstIp;
    }
}
