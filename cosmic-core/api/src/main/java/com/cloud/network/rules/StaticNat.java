package com.cloud.network.rules;

public interface StaticNat {

    long getAccountId();

    long getDomainId();

    long getNetworkId();

    long getSourceIpAddressId();

    String getDestIpAddress();

    String getSourceMacAddress();

    boolean isForRevoke();
}
