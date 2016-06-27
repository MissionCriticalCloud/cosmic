package com.cloud.usage;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "usage_load_balancer_policy")
public class UsageLoadBalancerPolicyVO implements InternalIdentity {

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "id")
    private long id;

    @Column(name = "created")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created = null;

    @Column(name = "deleted")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date deleted = null;

    protected UsageLoadBalancerPolicyVO() {
    }

    public UsageLoadBalancerPolicyVO(final long id, final long zoneId, final long accountId, final long domainId, final Date created, final Date deleted) {
        this.zoneId = zoneId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.id = id;
        this.created = created;
        this.deleted = deleted;
    }

    public long getZoneId() {
        return zoneId;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(final Date deleted) {
        this.deleted = deleted;
    }
}
