package com.cloud.storage;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
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

import com.google.gson.annotations.Expose;

@Entity
@Table(name = "snapshots")
public class SnapshotVO implements Snapshot {

    @Column(name = "data_center_id")
    long dataCenterId;
    @Column(name = "account_id")
    long accountId;
    @Column(name = "domain_id")
    long domainId;
    @Column(name = "volume_id")
    Long volumeId;
    @Column(name = "disk_offering_id")
    Long diskOfferingId;
    @Expose
    @Column(name = "name")
    String name;
    @Column(name = "snapshot_type")
    short snapshotType;
    @Column(name = "type_description")
    String typeDescription;
    @Column(name = "size")
    long size;
    @Column(name = GenericDao.CREATED_COLUMN)
    Date created;
    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;
    @Column(name = "hypervisor_type")
    @Enumerated(value = EnumType.STRING)
    HypervisorType hypervisorType;
    @Expose
    @Column(name = "version")
    String version;
    @Column(name = "uuid")
    String uuid;
    @Column(name = "min_iops")
    Long minIops;
    @Column(name = "max_iops")
    Long maxIops;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @Expose
    @Column(name = "status", updatable = true, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private State state;

    public SnapshotVO() {
        uuid = UUID.randomUUID().toString();
    }

    public SnapshotVO(final long dcId, final long accountId, final long domainId, final Long volumeId, final Long diskOfferingId, final String name, final short snapshotType,
                      final String typeDescription, final long size,
                      final Long minIops, final Long maxIops, final HypervisorType hypervisorType) {
        dataCenterId = dcId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.volumeId = volumeId;
        this.diskOfferingId = diskOfferingId;
        this.name = name;
        this.snapshotType = snapshotType;
        this.typeDescription = typeDescription;
        this.size = size;
        this.minIops = minIops;
        this.maxIops = maxIops;
        state = State.Allocated;
        this.hypervisorType = hypervisorType;
        version = "2.2";
        uuid = UUID.randomUUID().toString();
    }

    public static Type getSnapshotType(final String snapshotType) {
        for (final Type type : Type.values()) {
            if (type.equals(snapshotType)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(final Long volumeId) {
        this.volumeId = volumeId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Type getRecurringType() {
        if (snapshotType < 0 || snapshotType >= Type.values().length) {
            return null;
        }
        return Type.values()[snapshotType];
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    @Override
    public boolean isRecursive() {
        if (snapshotType >= Type.HOURLY.ordinal() && snapshotType <= Type.MONTHLY.ordinal()) {
            return true;
        }
        return false;
    }

    @Override
    public short getsnapshotType() {
        return snapshotType;
    }

    public void setState(final State state) {
        this.state = state;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public long getDiskOfferingId() {
        return diskOfferingId;
    }

    public void setSnapshotType(final short snapshotType) {
        this.snapshotType = snapshotType;
    }

    public long getSize() {
        return size;
    }

    public Long getMinIops() {
        return minIops;
    }

    public Long getMaxIops() {
        return maxIops;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public void setTypeDescription(final String typeDescription) {
        this.typeDescription = typeDescription;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public Date getRemoved() {
        return removed;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public Class<?> getEntityType() {
        return Snapshot.class;
    }
}
