package com.cloud.legacymodel.network.rules;

public class ProtocolAclRule extends AclRule {
    private final String type = "protocol";
    private int protocol;

    public ProtocolAclRule() {
        // Empty constructor for (de)serialization
    }

    public ProtocolAclRule(final String cidr, final boolean allowed, final int protocol) {
        super(cidr, allowed);
        this.protocol = protocol;
    }

    public int getProtocol() {
        return this.protocol;
    }

    public void setProtocol(final int protocol) {
        this.protocol = protocol;
    }
}
