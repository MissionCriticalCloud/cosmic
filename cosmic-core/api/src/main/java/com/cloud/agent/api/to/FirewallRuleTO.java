package com.cloud.agent.api.to;

import com.cloud.api.InternalIdentity;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.State;

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
public class FirewallRuleTO implements InternalIdentity {
    long id;
    String srcVlanTag;
    String srcIp;
    String protocol;
    int srcPort;
    boolean revoked;
    boolean alreadyAdded;
    FirewallRule.Purpose purpose;
    private List<String> sourceCidrList;
    private Integer icmpType;
    private Integer icmpCode;
    private FirewallRule.TrafficType trafficType;
    private String guestCidr;
    private boolean defaultEgressPolicy;

    protected FirewallRuleTO() {
    }

    public FirewallRuleTO(final long id, final String srcIp, final String protocol, final Integer srcPort, final boolean revoked, final boolean alreadyAdded, final FirewallRule.Purpose
            purpose, final List<String> sourceCidr, final Integer icmpType, final Integer icmpCode) {
        this(id, null, srcIp, protocol, srcPort, revoked, alreadyAdded, purpose, sourceCidr, icmpType, icmpCode);
    }

    public FirewallRuleTO(final long id, final String srcVlanTag, final String srcIp, final String protocol, final Integer srcPort, final boolean revoked, final boolean alreadyAdded, final
    FirewallRule.Purpose purpose, final List<String> sourceCidr, final Integer icmpType, final Integer icmpCode) {
        this.id = id;
        this.srcVlanTag = srcVlanTag;
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

    public FirewallRuleTO(final FirewallRule rule, final String srcVlanTag, final String srcIp) {
        this(rule.getId(),
                srcVlanTag,
                srcIp,
                rule.getProtocol(),
                rule.getSourcePort(),
                rule.getState() == State.Revoke,
                rule.getState() == State.Active,
                rule.getPurpose(),
                rule.getSourceCidrList(),
                rule.getIcmpType(),
                rule.getIcmpCode());
    }

    public FirewallRuleTO(final FirewallRule rule, final String srcIp) {
        this(rule.getId(),
                null,
                srcIp,
                rule.getProtocol(),
                rule.getSourcePort(),
                rule.getState() == State.Revoke,
                rule.getState() == State.Active,
                rule.getPurpose(),
                rule.getSourceCidrList(),
                rule.getIcmpType(),
                rule.getIcmpCode());
    }

    public FirewallRuleTO(final FirewallRule rule, final String srcVlanTag, final String srcIp, final FirewallRule.Purpose purpose) {
        this(rule.getId(),
                srcVlanTag,
                srcIp,
                rule.getProtocol(),
                rule.getSourcePort(),
                rule.getState() == State.Revoke,
                rule.getState() == State.Active,
                purpose,
                rule.getSourceCidrList(),
                rule.getIcmpType(),
                rule.getIcmpCode());
    }

    public FirewallRuleTO(final FirewallRule rule, final String srcVlanTag, final String srcIp, final FirewallRule.Purpose purpose, final FirewallRule.TrafficType trafficType) {
        this(rule.getId(),
                srcVlanTag,
                srcIp,
                rule.getProtocol(),
                rule.getSourcePort(),
                rule.getState() == State.Revoke,
                rule.getState() == State.Active,
                purpose,
                rule.getSourceCidrList(),
                rule.getIcmpType(),
                rule.getIcmpCode());
        this.trafficType = trafficType;
    }

    public FirewallRuleTO(final FirewallRule rule, final String srcVlanTag, final String srcIp, final FirewallRule.Purpose purpose, final FirewallRule.TrafficType trafficType,
                          final boolean defaultEgressPolicy) {
        this(rule.getId(),
                srcVlanTag,
                srcIp,
                rule.getProtocol(),
                rule.getSourcePort(),
                rule.getState() == State.Revoke,
                rule.getState() == State.Active,
                purpose,
                rule.getSourceCidrList(),
                rule.getIcmpType(),
                rule.getIcmpCode());
        this.trafficType = trafficType;
        this.defaultEgressPolicy = defaultEgressPolicy;
    }

    public FirewallRuleTO(final FirewallRule rule, final String srcVlanTag, final String srcIp, final FirewallRule.Purpose purpose, final boolean revokeState, final boolean
            alreadyAdded) {
        this(rule.getId(),
                srcVlanTag,
                srcIp,
                rule.getProtocol(),
                rule.getSourcePort(),
                revokeState,
                alreadyAdded,
                purpose,
                rule.getSourceCidrList(),
                rule.getIcmpType(),
                rule.getIcmpCode());
    }

    public FirewallRuleTO(final FirewallRule rule, final String guestVlanTag, final FirewallRule.TrafficType trafficType, final String guestCidr, final boolean defaultEgressPolicy) {
        this(rule.getId(),
                guestVlanTag,
                null,
                rule.getProtocol(),
                rule.getSourcePort(),

                rule.getState() == State.Revoke,
                rule.getState() == State.Active,
                rule.getPurpose(),
                rule.getSourceCidrList(),
                rule.getIcmpType(),
                rule.getIcmpCode());
        this.trafficType = trafficType;
        this.defaultEgressPolicy = defaultEgressPolicy;
        this.guestCidr = guestCidr;
    }

    public FirewallRule.TrafficType getTrafficType() {
        return trafficType;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getSrcVlanTag() {
        return srcVlanTag;
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

    public boolean isDefaultEgressPolicy() {
        return defaultEgressPolicy;
    }

    public String getGuestCidr() {
        return guestCidr;
    }
}
