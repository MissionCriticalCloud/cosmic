package com.cloud.usage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "usage_security_group")
public class UsageSecurityGroupVO {

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "vm_instance_id")
    private long vmInstanceId;

    @Column(name = "security_group_id")
    private Long securityGroupId;

    @Column(name = "created")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created = null;

    @Column(name = "deleted")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date deleted = null;

    public UsageSecurityGroupVO() {
    }

    public UsageSecurityGroupVO(final long zoneId, final long accountId, final long domainId, final long vmInstanceId, final long securityGroupId, final Date created, final Date
            deleted) {
        this.zoneId = zoneId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.vmInstanceId = vmInstanceId;
        this.securityGroupId = securityGroupId;
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

    public long getVmInstanceId() {
        return vmInstanceId;
    }

    public Long getSecurityGroupId() {
        return securityGroupId;
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
