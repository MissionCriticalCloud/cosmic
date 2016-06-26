package com.cloud.usage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "usage_network_offering")
public class UsageNetworkOfferingVO {

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "vm_instance_id")
    private long vmInstanceId;

    @Column(name = "network_offering_id")
    private Long networkOfferingId;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @Column(name = "created")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created = null;

    @Column(name = "deleted")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date deleted = null;

    @Column(name = "nic_id")
    private Long nicId;

    protected UsageNetworkOfferingVO() {
    }

    public UsageNetworkOfferingVO(final long zoneId, final long accountId, final long domainId, final long vmInstanceId, final long networkOfferingId, final long nicId, final
    boolean isDefault, final Date created,
                                  final Date deleted) {
        this.zoneId = zoneId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.vmInstanceId = vmInstanceId;
        this.networkOfferingId = networkOfferingId;
        this.isDefault = isDefault;
        this.created = created;
        this.deleted = deleted;
        this.nicId = nicId;
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

    public Long getNetworkOfferingId() {
        return networkOfferingId;
    }

    public boolean isDefault() {
        return isDefault;
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

    public Long getNicId() {
        return nicId;
    }

    public void setNicId(final Long nicId) {
        this.nicId = nicId;
    }
}
