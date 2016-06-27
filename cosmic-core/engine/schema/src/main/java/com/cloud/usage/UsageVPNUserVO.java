package com.cloud.usage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "usage_vpn_user")
public class UsageVPNUserVO {

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "user_name")
    private String username;

    @Column(name = "created")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created = null;

    @Column(name = "deleted")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date deleted = null;

    protected UsageVPNUserVO() {
    }

    public UsageVPNUserVO(final long zoneId, final long accountId, final long domainId, final long userId, final String username, final Date created, final Date deleted) {
        this.zoneId = zoneId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.userId = userId;
        this.username = username;
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

    public Date getCreated() {
        return created;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(final Date deleted) {
        this.deleted = deleted;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(final long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
