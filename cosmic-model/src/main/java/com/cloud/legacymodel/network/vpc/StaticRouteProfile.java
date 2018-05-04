package com.cloud.legacymodel.network.vpc;

import java.util.Objects;

public class StaticRouteProfile implements StaticRoute {
    String ipAddress;
    private final long id;
    private final String uuid;
    private final String targetCidr;
    private final long accountId;
    private final long domainId;
    private final StaticRoute.State state;
    private final long vpcId;
    private final int metric;

    public StaticRouteProfile(final StaticRoute staticRoute) {
        id = staticRoute.getId();
        uuid = staticRoute.getUuid();
        targetCidr = staticRoute.getCidr();
        accountId = staticRoute.getAccountId();
        domainId = staticRoute.getDomainId();
        state = staticRoute.getState();
        vpcId = staticRoute.getVpcId();
        ipAddress = staticRoute.getGwIpAddress();
        metric = staticRoute.getMetric();
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
    public Integer getMetric() {
        return metric;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final StaticRouteProfile that = (StaticRouteProfile) o;
        return id == that.id &&
                accountId == that.accountId &&
                domainId == that.domainId &&
                vpcId == that.vpcId &&
                metric == that.metric &&
                Objects.equals(ipAddress, that.ipAddress) &&
                Objects.equals(uuid, that.uuid) &&
                Objects.equals(targetCidr, that.targetCidr) &&
                state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, id, uuid, targetCidr, accountId, domainId, state, vpcId, metric);
    }
}
