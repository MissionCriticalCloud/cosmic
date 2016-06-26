package com.cloud.network.dao;

import com.cloud.network.Site2SiteVpnGateway;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = ("s2s_vpn_gateway"))
public class Site2SiteVpnGatewayVO implements Site2SiteVpnGateway {
    @Column(name = "display", updatable = true, nullable = false)
    protected boolean display = true;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "addr_id")
    private long addrId;
    @Column(name = "vpc_id")
    private long vpcId;
    @Column(name = "domain_id")
    private Long domainId;
    @Column(name = "account_id")
    private Long accountId;
    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    public Site2SiteVpnGatewayVO() {
    }

    public Site2SiteVpnGatewayVO(final long accountId, final long domainId, final long addrId, final long vpcId) {
        uuid = UUID.randomUUID().toString();
        setAddrId(addrId);
        setVpcId(vpcId);
        this.accountId = accountId;
        this.domainId = domainId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getAddrId() {
        return addrId;
    }

    @Override
    public long getVpcId() {
        return vpcId;
    }

    public void setVpcId(final long vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }

    public void setAddrId(final long addrId) {
        this.addrId = addrId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public Class<?> getEntityType() {
        return Site2SiteVpnGateway.class;
    }
}
