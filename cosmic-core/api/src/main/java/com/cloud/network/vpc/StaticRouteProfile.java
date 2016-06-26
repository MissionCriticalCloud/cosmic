package com.cloud.network.vpc;

public class StaticRouteProfile implements StaticRoute {
    String ipAddress;
    private final long id;
    private final String uuid;
    private final String targetCidr;
    private final long accountId;
    private final long domainId;
    private final StaticRoute.State state;
    private final long vpcId;

    public StaticRouteProfile(final StaticRoute staticRoute) {
        id = staticRoute.getId();
        uuid = staticRoute.getUuid();
        targetCidr = staticRoute.getCidr();
        accountId = staticRoute.getAccountId();
        domainId = staticRoute.getDomainId();
        state = staticRoute.getState();
        vpcId = staticRoute.getVpcId();
        ipAddress = staticRoute.getGwIpAddress();
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
    public String getCidr() {
        return targetCidr;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Long getVpcId() {
        return vpcId;
    }

    public String getGwIpAddress() {
        return ipAddress;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public String getIp4Address() {
        return ipAddress;
    }

    @Override
    public Class<?> getEntityType() {
        return StaticRoute.class;
    }
}
