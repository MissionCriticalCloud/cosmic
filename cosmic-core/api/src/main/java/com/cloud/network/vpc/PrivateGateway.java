package com.cloud.network.vpc;

public interface PrivateGateway extends VpcGateway {
    long getPhysicalNetworkId();
}
