package com.cloud.network.rules;

import java.util.List;

public class StaticNatRuleImpl implements StaticNatRule {
    long id;
    String xid;
    String uuid;
    String protocol;
    int portStart;
    int portEnd;
    State state;
    long accountId;
    long domainId;
    long networkId;
    long sourceIpAddressId;
    String destIpAddress;
    boolean forDisplay;

    public StaticNatRuleImpl(final FirewallRuleVO rule, final String dstIp) {
        id = rule.getId();
        xid = rule.getXid();
        uuid = rule.getUuid();
        protocol = rule.getProtocol();
        portStart = rule.getSourcePortStart().intValue();
        portEnd = rule.getSourcePortEnd().intValue();
        state = rule.getState();
        accountId = rule.getAccountId();
        domainId = rule.getDomainId();
        networkId = rule.getNetworkId();
        sourceIpAddressId = rule.getSourceIpAddressId();
        destIpAddress = dstIp;
        forDisplay = rule.isDisplay();
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDestIpAddress() {
        return destIpAddress;
    }

    @Override
    public String getXid() {
        return xid;
    }

    @Override
    public Integer getSourcePortStart() {
        return portStart;
    }

    @Override
    public Integer getSourcePortEnd() {
        return portEnd;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Purpose getPurpose() {
        return Purpose.StaticNat;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public long getNetworkId() {
        return networkId;
    }

    @Override
    public Long getSourceIpAddressId() {
        return sourceIpAddressId;
    }

    @Override
    public Integer getIcmpCode() {
        return null;
    }

    @Override
    public Integer getIcmpType() {
        return null;
    }

    @Override
    public List<String> getSourceCidrList() {
        return null;
    }

    @Override
    public Long getRelated() {
        return null;
    }

    @Override
    public FirewallRuleType getType() {
        return FirewallRuleType.User;
    }

    @Override
    public TrafficType getTrafficType() {
        return null;
    }

    @Override
    public boolean isDisplay() {
        return forDisplay;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public Class<?> getEntityType() {
        return FirewallRule.class;
    }
}
