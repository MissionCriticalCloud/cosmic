package com.cloud.vm.snapshot;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.engine.subsystem.api.storage.VMSnapshotOptions;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "vm_snapshots")
public class VMSnapshotVO implements VMSnapshot {
    @Column(name = "update_count", updatable = true, nullable = false)
    protected long updatedCount;
    @Id
    @TableGenerator(name = "vm_snapshots_sq",
            table = "sequence",
            pkColumnName = "name",
            valueColumnName = "value",
            pkColumnValue = "vm_snapshots_seq",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    Long id;
    @Column(name = "uuid")
    String uuid = UUID.randomUUID().toString();
    @Column(name = "name")
    String name;
    @Column(name = "display_name")
    String displayName;
    @Column(name = "description")
    String description;
    @Column(name = "vm_id")
    long vmId;
    @Column(name = "account_id")
    long accountId;
    @Column(name = "domain_id")
    long domainId;
    @Column(name = "vm_snapshot_type")
    @Enumerated(EnumType.STRING)
    VMSnapshot.Type type;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;

    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;

    @Column(name = "current")
    Boolean current;

    @Column(name = "parent")
    Long parent;

    @Column(name = "updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date updated;
    @Transient
    VMSnapshotOptions options;
    @Column(name = "state", updatable = true, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private State state;

    public VMSnapshotVO() {

    }

    public VMSnapshotVO(final Long accountId, final Long domainId, final Long vmId, final String description, final String vmSnapshotName, final String vsDisplayName, final Long
            serviceOfferingId, final Type type,
                        final Boolean current) {
        this.accountId = accountId;
        this.domainId = domainId;
        this.vmId = vmId;
        state = State.Allocated;
        this.description = description;
        name = vmSnapshotName;
        displayName = vsDisplayName;
        this.type = type;
        this.current = current;
    }

    public VMSnapshotOptions getOptions() {
        return options;
    }

    public void setOptions(final VMSnapshotOptions options) {
        this.options = options;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getVmId() {
        return vmId;
    }

    public void setVmId(final Long vmId) {
        this.vmId = vmId;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Long getParent() {
        return parent;
    }

    public void setParent(final Long parent) {
        this.parent = parent;
    }

    @Override
    public Boolean getCurrent() {
        return current;
    }

    public void setCurrent(final Boolean current) {
        this.current = current;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public long getUpdatedCount() {
        return updatedCount;
    }

    @Override
    public void incrUpdatedCount() {
        updatedCount++;
    }

    @Override
    public Date getUpdated() {
        return updated;
    }

    @Override
    public Date getRemoved() {
        return removed;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public Class<?> getEntityType() {
        return VMSnapshot.class;
    }
}
