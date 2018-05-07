package com.cloud.legacymodel.network.rules;

public class IcmpAclRule extends AclRule {
    private final String type = "icmp";
    private int icmpType;
    private int icmpCode;

    public IcmpAclRule() {
        // Empty constructor for (de)serialization
    }

    public IcmpAclRule(final String cidr, final boolean allowed, final int icmpType, final int icmpCode) {
        super(cidr, allowed);
        this.icmpType = icmpType;
        this.icmpCode = icmpCode;
    }

    public int getIcmpType() {
        return this.icmpType;
    }

    public void setIcmpType(final int icmpType) {
        this.icmpType = icmpType;
    }

    public int getIcmpCode() {
        return this.icmpCode;
    }

    public void setIcmpCode(final int icmpCode) {
        this.icmpCode = icmpCode;
    }
}
