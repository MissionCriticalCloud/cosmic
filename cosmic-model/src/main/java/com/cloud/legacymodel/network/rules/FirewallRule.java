package com.cloud.legacymodel.network.rules;

import java.util.List;

public class FirewallRule {
    private long id;
    private String srcVlanTag;
    private String srcIp;
    private String protocol;
    private int[] srcPortRange;
    private boolean revoked;
    private boolean alreadyAdded;
    private List<String> sourceCidrList;
    private String purpose;
    private Integer icmpType;
    private Integer icmpCode;
    private String trafficType;
    private String guestCidr;
    private boolean defaultEgressPolicy;
    private String type;

    public FirewallRule() {
        // Empty constructor for (de)serialization
    }

    public FirewallRule(final long id, final String srcVlanTag, final String srcIp, final String protocol, final int[] srcPortRange, final boolean revoked, final boolean
            alreadyAdded, final List<String> sourceCidrList,
                        final String purpose, final Integer icmpType, final Integer icmpCode, final String trafficType, final String guestCidr, final boolean defaultEgressPolicy) {
        this.id = id;
        this.srcVlanTag = srcVlanTag;
        this.srcIp = srcIp;
        this.protocol = protocol;
        this.srcPortRange = srcPortRange;
        this.revoked = revoked;
        this.alreadyAdded = alreadyAdded;
        this.sourceCidrList = sourceCidrList;
        this.purpose = purpose;
        this.icmpType = icmpType;
        this.icmpCode = icmpCode;
        this.trafficType = trafficType;
        this.guestCidr = guestCidr;
        this.defaultEgressPolicy = defaultEgressPolicy;
    }

    public long getId() {
        return this.id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getSrcVlanTag() {
        return this.srcVlanTag;
    }

    public void setSrcVlanTag(final String srcVlanTag) {
        this.srcVlanTag = srcVlanTag;
    }

    public String getSrcIp() {
        return this.srcIp;
    }

    public void setSrcIp(final String srcIp) {
        this.srcIp = srcIp;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public int[] getSrcPortRange() {
        return this.srcPortRange;
    }

    public void setSrcPortRange(final int[] srcPortRange) {
        this.srcPortRange = srcPortRange;
    }

    public boolean isRevoked() {
        return this.revoked;
    }

    public void setRevoked(final boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isAlreadyAdded() {
        return this.alreadyAdded;
    }

    public void setAlreadyAdded(final boolean alreadyAdded) {
        this.alreadyAdded = alreadyAdded;
    }

    public List<String> getSourceCidrList() {
        return this.sourceCidrList;
    }

    public void setSourceCidrList(final List<String> sourceCidrList) {
        this.sourceCidrList = sourceCidrList;
    }

    public String getPurpose() {
        return this.purpose;
    }

    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    public Integer getIcmpType() {
        return this.icmpType;
    }

    public void setIcmpType(final Integer icmpType) {
        this.icmpType = icmpType;
    }

    public Integer getIcmpCode() {
        return this.icmpCode;
    }

    public void setIcmpCode(final Integer icmpCode) {
        this.icmpCode = icmpCode;
    }

    public String getTrafficType() {
        return this.trafficType;
    }

    public void setTrafficType(final String trafficType) {
        this.trafficType = trafficType;
    }

    public String getGuestCidr() {
        return this.guestCidr;
    }

    public void setGuestCidr(final String guestCidr) {
        this.guestCidr = guestCidr;
    }

    public boolean isDefaultEgressPolicy() {
        return this.defaultEgressPolicy;
    }

    public void setDefaultEgressPolicy(final boolean defaultEgressPolicy) {
        this.defaultEgressPolicy = defaultEgressPolicy;
    }
}
