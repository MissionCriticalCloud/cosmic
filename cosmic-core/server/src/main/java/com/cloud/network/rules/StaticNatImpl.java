package com.cloud.network.rules;

public class StaticNatImpl implements StaticNat {
    long accountId;
    long domainId;
    long networkId;
    long sourceIpAddressId;
    String destIpAddress;
    String sourceMacAddress;
    boolean forRevoke;

    public StaticNatImpl(final long accountId, final long domainId, final long networkId, final long sourceIpAddressId, final String destIpAddress, final boolean forRevoke) {
        super();
        this.accountId = accountId;
        this.domainId = domainId;
        this.networkId = networkId;
        this.sourceIpAddressId = sourceIpAddressId;
        this.destIpAddress = destIpAddress;
        this.sourceMacAddress = null;
        this.forRevoke = forRevoke;
    }

    public StaticNatImpl(final long accountId, final long domainId, final long networkId, final long sourceIpAddressId, final String destIpAddress, final String
            sourceMacAddress, final boolean forRevoke) {
        super();
        this.accountId = accountId;
        this.domainId = domainId;
        this.networkId = networkId;
        this.sourceIpAddressId = sourceIpAddressId;
        this.destIpAddress = destIpAddress;
        this.sourceMacAddress = sourceMacAddress;
        this.forRevoke = forRevoke;
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
    public long getNetworkId() {
        return networkId;
    }

    @Override
    public long getSourceIpAddressId() {
        return sourceIpAddressId;
    }

    @Override
    public String getDestIpAddress() {
        return destIpAddress;
    }

    @Override
    public String getSourceMacAddress() {
        return sourceMacAddress;
    }

    @Override
    public boolean isForRevoke() {
        return forRevoke;
    }
}
