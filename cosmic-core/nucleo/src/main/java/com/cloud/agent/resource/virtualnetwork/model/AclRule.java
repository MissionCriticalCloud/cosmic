package com.cloud.agent.resource.virtualnetwork.model;

public abstract class AclRule {
    private String cidr;
    private boolean allowed;

    protected AclRule() {
        // Empty constructor for (de)serialization
    }

    protected AclRule(final String cidr, final boolean allowed) {
        this.cidr = cidr;
        this.allowed = allowed;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(final String cidr) {
        this.cidr = cidr;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(final boolean allowed) {
        this.allowed = allowed;
    }
}

