package com.cloud.usage;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "usage_storage")
public class UsageStorageVO implements InternalIdentity {

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "id")
    private long id;

    @Column(name = "storage_type")
    private int storageType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "size")
    private long size;

    @Column(name = "created")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created = null;

    @Column(name = "deleted")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date deleted = null;

    @Column(name = "virtual_size")
    private Long virtualSize;

    protected UsageStorageVO() {
    }

    public UsageStorageVO(final long id, final long zoneId, final long accountId, final long domainId, final int storageType, final Long sourceId, final long size, final Date
            created, final Date deleted) {
        this.zoneId = zoneId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.id = id;
        this.storageType = storageType;
        this.sourceId = sourceId;
        this.size = size;
        this.created = created;
        this.deleted = deleted;
    }

    public UsageStorageVO(final long id, final long zoneId, final long accountId, final long domainId, final int storageType, final Long sourceId, final long size, final Long
            virtualSize, final Date created, final Date deleted) {
        this.zoneId = zoneId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.id = id;
        this.storageType = storageType;
        this.sourceId = sourceId;
        this.size = size;
        this.virtualSize = virtualSize;
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

    public int getStorageType() {
        return storageType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public long getSize() {
        return size;
    }

    public Long getVirtualSize() {
        return virtualSize;
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
