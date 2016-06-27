package com.cloud.storage;

import com.cloud.utils.db.GenericDaoBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Join table for storage pools and hosts
 */
@Entity
@Table(name = "storage_pool_host_ref")
public class StoragePoolHostVO implements StoragePoolHostAssoc {
    @Column(name = GenericDaoBase.CREATED_COLUMN)
    private Date created = null;
    @Column(name = "last_updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastUpdated = null;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "pool_id")
    private long poolId;
    @Column(name = "host_id")
    private long hostId;
    @Column(name = "local_path")
    private String localPath;

    public StoragePoolHostVO() {
        super();
    }

    public StoragePoolHostVO(final long poolId, final long hostId, final String localPath) {
        this.poolId = poolId;
        this.hostId = hostId;
        this.localPath = localPath;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setLastUpdated(final Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setPoolId(final long poolId) {
        this.poolId = poolId;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }

    @Override
    public long getHostId() {
        return hostId;
    }

    @Override
    public long getPoolId() {
        return poolId;
    }

    @Override
    public String getLocalPath() {
        return localPath;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLocalPath(final String localPath) {
        this.localPath = localPath;
    }

    @Override
    public long getId() {
        return id;
    }
}
