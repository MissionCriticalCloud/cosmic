package com.cloud.network.vpc;

public class PrivateGatewayProfile implements PrivateGateway {
    VpcGateway vpcGateway;
    long physicalNetworkId;

    /**
     * @param vpcGateway
     * @param physicalNetworkId TODO
     */
    public PrivateGatewayProfile(final VpcGateway vpcGateway, final long physicalNetworkId) {
        super();
        this.vpcGateway = vpcGateway;
        this.physicalNetworkId = physicalNetworkId;
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
    public String getGateway() {
        return vpcGateway.getGateway();
    }

    @Override
    public String getNetmask() {
        return vpcGateway.getNetmask();
    }

    @Override
    public String getBroadcastUri() {
        return vpcGateway.getBroadcastUri();
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
    public long getPhysicalNetworkId() {
        return physicalNetworkId;
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
