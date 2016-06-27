package com.cloud.usage;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "usage_ip_address")
public class UsageIPAddressVO implements InternalIdentity {
    @Column(name = "account_id")
    private long accountId;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "id")
    private long id;

    @Column(name = "public_ip_address")
    private String address = null;

    @Column(name = "is_source_nat")
    private boolean isSourceNat = false;

    @Column(name = "is_system")
    private boolean isSystem = false;

    @Column(name = "assigned")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date assigned = null;

    @Column(name = "released")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date released = null;

    protected UsageIPAddressVO() {
    }

    public UsageIPAddressVO(final long id, final long accountId, final long domainId, final long zoneId, final String address, final boolean isSourceNat, final boolean isSystem,
                            final Date assigned, final Date released) {
        this.id = id;
        this.accountId = accountId;
        this.domainId = domainId;
        this.zoneId = zoneId;
        this.address = address;
        this.isSourceNat = isSourceNat;
        this.isSystem = isSystem;
        this.assigned = assigned;
        this.released = released;
    }

    public UsageIPAddressVO(final long accountId, final String address, final Date assigned, final Date released) {
        this.accountId = accountId;
        this.address = address;
        this.assigned = assigned;
        this.released = released;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getDomainId() {
        return domainId;
    }

    public long getZoneId() {
        return zoneId;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public boolean isSourceNat() {
        return isSourceNat;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public Date getAssigned() {
        return assigned;
    }

    public Date getReleased() {
        return released;
    }

    public void setReleased(final Date released) {
        this.released = released;
    }
}
