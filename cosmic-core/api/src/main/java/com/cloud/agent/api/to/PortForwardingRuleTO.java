package com.cloud.agent.api.to;

import com.cloud.api.InternalIdentity;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.State;
import com.cloud.network.rules.PortForwardingRule;

import java.util.List;

/**
 * FirewallRuleTO transfers a port range for an ip to be opened.
 * <p>
 * There are essentially three states transferred with each state.
 * sent multiple times to the destination.  If the rule is not on
 * 2. alreadyAdded - the rule has been successfully added before.  Rules
 * in this state are sent for completeness and optimization.
 * If the rule already exists on the destination, the destination should
 * reply the rule is successfully applied.
 * <p>
 * - srcPort: port range to open.
 * - protocol: protocol to open for.  Usually tcp and udp.
 */
public class PortForwardingRuleTO implements InternalIdentity {
    private long id;
    private String srcIp;
    private String protocol;
    private int srcPort;
    private boolean revoked;
    private boolean alreadyAdded;
    private FirewallRule.Purpose purpose;
    private List<String> sourceCidrList;
    private Integer icmpType;
    private Integer icmpCode;
    private FirewallRule.TrafficType trafficType;

    private String dstIp;
    private int dstPort;

    public PortForwardingRuleTO(final PortForwardingRule rule, final String srcIp) {
        this(rule.getId(),
                srcIp,
                rule.getProtocol(),
                rule.getSourcePort(),
                rule.getState() == State.Revoke,
                rule.getState() == State.Active,
                rule.getPurpose(),
                rule.getSourceCidrList(),
                rule.getIcmpType(),
                rule.getIcmpCode());
        this.dstIp = rule.getDestinationIpAddress().addr();
        this.dstPort = rule.getDestinationPortStart();
    }

    public PortForwardingRuleTO(final long id, final String srcIp, final int srcPort, final String dstIp, final int dstPort, final String protocol, final boolean revoked, final boolean
            alreadyAdded) {
        this(id, srcIp, protocol, srcPort, revoked, alreadyAdded, FirewallRule.Purpose.PortForwarding, null, 0, 0);
        this.dstIp = dstIp;
        this.dstPort = dstPort;
    }

    public String getDstIp() {
        return dstIp;
    }

    public int getDstPort() {
        return dstPort;
    }

    PortForwardingRuleTO() {
    }

    PortForwardingRuleTO(final long id, final String srcIp, final String protocol, final Integer srcPort, final boolean revoked, final boolean alreadyAdded, final FirewallRule.Purpose
            purpose, final List<String> sourceCidr, final Integer icmpType, final Integer icmpCode) {
        this.id = id;
        this.srcIp = srcIp;
        this.protocol = protocol;
        this.srcPort = srcPort;
        this.revoked = revoked;
        this.alreadyAdded = alreadyAdded;
        this.purpose = purpose;
        this.sourceCidrList = sourceCidr;
        this.icmpType = icmpType;
        this.icmpCode = icmpCode;
        this.trafficType = null;
    }

    public FirewallRule.TrafficType getTrafficType() {
        return trafficType;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public Integer getIcmpType() {
        return icmpType;
    }

    public Integer getIcmpCode() {
        return icmpCode;
    }

    public boolean revoked() {
        return revoked;
    }

    public List<String> getSourceCidrList() {
        return sourceCidrList;
    }

    public boolean isAlreadyAdded() {
        return alreadyAdded;
    }

    public FirewallRule.Purpose getPurpose() {
        return purpose;
    }
}
