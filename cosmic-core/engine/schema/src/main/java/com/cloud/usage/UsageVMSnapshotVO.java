package com.cloud.usage;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "usage_vmsnapshot")
public class UsageVMSnapshotVO implements InternalIdentity {

    @Column(name = "id")
    // volumeId
    private long id;

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "vm_id")
    private long vmId;

    @Column(name = "disk_offering_id")
    private Long diskOfferingId;

    @Column(name = "size")
    private long size;

    @Column(name = "created")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created = null;

    @Column(name = "processed")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date processed;

    protected UsageVMSnapshotVO() {
    }

    public UsageVMSnapshotVO(final long id, final long zoneId, final long accountId, final long domainId, final long vmId, final Long diskOfferingId, final long size, final Date
            created, final Date processed) {
        this.zoneId = zoneId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.diskOfferingId = diskOfferingId;
        this.id = id;
        this.size = size;
        this.created = created;
        this.vmId = vmId;
        this.processed = processed;
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

    public Long getDiskOfferingId() {
        return diskOfferingId;
    }

    public long getSize() {
        return size;
    }

    public Date getProcessed() {
        return processed;
    }

    public void setProcessed(final Date processed) {
        this.processed = processed;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public long getVmId() {
        return vmId;
    }

    @Override
    public long getId() {
        return this.id;
    }
}
