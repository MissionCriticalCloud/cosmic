package org.apache.cloudstack.framework.jobs.impl;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "sync_queue")
public class SyncQueueVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "sync_objtype")
    private String syncObjType;

    @Column(name = "sync_objid")
    private Long syncObjId;

    @Column(name = "queue_proc_number")
    private Long lastProcessNumber;

    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "queue_size")
    private long queueSize = 0;

    @Column(name = "queue_size_limit")
    private long queueSizeLimit = 0;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("SyncQueueVO {id:").append(getId());
        sb.append(", syncObjType: ").append(getSyncObjType());
        sb.append(", syncObjId: ").append(getSyncObjId());
        sb.append(", lastProcessNumber: ").append(getLastProcessNumber());
        sb.append(", lastUpdated: ").append(getLastUpdated());
        sb.append(", created: ").append(getCreated());
        sb.append(", count: ").append(getQueueSize());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public long getId() {
        return id;
    }

    public String getSyncObjType() {
        return syncObjType;
    }

    public void setSyncObjType(final String syncObjType) {
        this.syncObjType = syncObjType;
    }

    public Long getSyncObjId() {
        return syncObjId;
    }

    public void setSyncObjId(final Long syncObjId) {
        this.syncObjId = syncObjId;
    }

    public Long getLastProcessNumber() {
        return lastProcessNumber;
    }

    public void setLastProcessNumber(final Long number) {
        lastProcessNumber = number;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public long getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(final long queueSize) {
        this.queueSize = queueSize;
    }

    public void setLastUpdated(final Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public long getQueueSizeLimit() {
        return queueSizeLimit;
    }

    public void setQueueSizeLimit(final long queueSizeLimit) {
        this.queueSizeLimit = queueSizeLimit;
    }
}
