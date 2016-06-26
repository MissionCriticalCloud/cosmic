package com.cloud.network.vpc;

import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "static_routes")
public class StaticRouteVO implements StaticRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "uuid")
    String uuid;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "state")
    State state;
    @Column(name = "account_id")
    long accountId;
    @Column(name = "domain_id")
    long domainId;
    @Column(name = "gateway_ip_address")
    String gwIpAddress;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Column(name = "cidr")
    private String cidr;
    @Column(name = "vpc_id")
    private Long vpcId;

    protected StaticRouteVO() {
        uuid = UUID.randomUUID().toString();
    }

    /**
     * @param cidr
     * @param vpcId
     * @param accountId TODO
     * @param domainId  TODO
     */
    public StaticRouteVO(final String cidr, final Long vpcId, final long accountId, final long domainId, final String gwIpAddress) {
        this.cidr = cidr;
        state = State.Staged;
        this.vpcId = vpcId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.gwIpAddress = gwIpAddress;
        uuid = UUID.randomUUID().toString();
    }

    @Override
    public String getCidr() {
        return cidr;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Long getVpcId() {
        return vpcId;
    }

    @Override
    public String getGwIpAddress() {
        return gwIpAddress;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public long getId() {
        return id;
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
    public String toString() {
        final StringBuilder buf = new StringBuilder("StaticRoute[");
        buf.append(uuid).append("|").append(cidr).append("]");
        return buf.toString();
    }

    @Override
    public Class<?> getEntityType() {
        return StaticRoute.class;
    }
}
