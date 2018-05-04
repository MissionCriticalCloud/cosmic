package com.cloud.network.vpc;

import com.cloud.legacymodel.network.vpc.PrivateGateway;
import com.cloud.legacymodel.network.vpc.VpcGateway;

public class PrivateGatewayProfile implements PrivateGateway {

    private final VpcGateway vpcGateway;

    /**
     * @param vpcGateway
     */
    public PrivateGatewayProfile(final VpcGateway vpcGateway) {
        super();
        this.vpcGateway = vpcGateway;
    }

    @Override
    public long getId() {
        return vpcGateway.getId();
    }

    @Override
    public String getIp4Address() {
        return vpcGateway.getIp4Address();
    }

    @Override
    public Type getType() {
        return vpcGateway.getType();
    }

    @Override
    public Long getVpcId() {
        return vpcGateway.getVpcId();
    }

    @Override
    public long getZoneId() {
        return vpcGateway.getZoneId();
    }

    @Override
    public long getNetworkId() {
        return vpcGateway.getNetworkId();
    }

    @Override
    public State getState() {
        return vpcGateway.getState();
    }

    @Override
    public boolean getSourceNat() {
        return vpcGateway.getSourceNat();
    }

    @Override
    public long getNetworkACLId() {
        return vpcGateway.getNetworkACLId();
    }

    @Override
    public String getUuid() {
        return vpcGateway.getUuid();
    }

    @Override
    public long getAccountId() {
        return vpcGateway.getAccountId();
    }

    @Override
    public long getDomainId() {
        return vpcGateway.getDomainId();
    }

    @Override
    public Class<?> getEntityType() {
        return VpcGateway.class;
    }
}
