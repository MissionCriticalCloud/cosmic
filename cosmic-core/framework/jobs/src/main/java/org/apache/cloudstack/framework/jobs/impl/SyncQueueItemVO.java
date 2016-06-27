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
@Table(name = "sync_queue_item")
public class SyncQueueItemVO implements SyncQueueItem, InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id = null;

    @Column(name = "queue_id")
    private Long queueId;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "queue_proc_msid")
    private Long lastProcessMsid;

    @Column(name = "queue_proc_number")
    private Long lastProcessNumber;

    @Column(name = "queue_proc_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastProcessTime;

    @Column(name = "created")
    private Date created;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("SyncQueueItemVO {id:").append(getId()).append(", queueId: ").append(getQueueId());
        sb.append(", contentType: ").append(getContentType());
        sb.append(", contentId: ").append(getContentId());
        sb.append(", lastProcessMsid: ").append(getLastProcessMsid());
        sb.append(", lastprocessNumber: ").append(getLastProcessNumber());
        sb.append(", lastProcessTime: ").append(getLastProcessTime());
        sb.append(", created: ").append(getCreated());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Long getQueueId() {
        return queueId;
    }

    public void setQueueId(final Long queueId) {
        this.queueId = queueId;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    @Override
    public Long getContentId() {
        return contentId;
    }

    public void setContentId(final Long contentId) {
        this.contentId = contentId;
    }

    public Long getLastProcessMsid() {
        return lastProcessMsid;
    }

    public void setLastProcessMsid(final Long lastProcessMsid) {
        this.lastProcessMsid = lastProcessMsid;
    }

    public Long getLastProcessNumber() {
        return lastProcessNumber;
    }

    public void setLastProcessNumber(final Long lastProcessNumber) {
        this.lastProcessNumber = lastProcessNumber;
    }

    public Date getLastProcessTime() {
        return lastProcessTime;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setLastProcessTime(final Date lastProcessTime) {
        this.lastProcessTime = lastProcessTime;
    }
}
