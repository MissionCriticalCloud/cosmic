package com.cloud.alert;

import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "alert")
public class AlertVO implements Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "type")
    private short type;

    @Column(name = "cluster_id")
    private Long clusterId = null;

    @Column(name = "pod_id")
    private Long podId = null;

    @Column(name = "data_center_id")
    private long dataCenterId = 0;

    @Column(name = "subject", length = 999)
    private String subject;

    @Column(name = "sent_count")
    private int sentCount = 0;

    @Column(name = GenericDao.CREATED_COLUMN)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_sent", updatable = true, nullable = true)
    private Date lastSent;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "resolved", updatable = true, nullable = true)
    private Date resolved;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "archived")
    private boolean archived;

    @Column(name = "name")
    private String name;

    public AlertVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public short getType() {
        return type;
    }

    public void setType(final short type) {
        this.type = type;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    @Override
    public Long getPodId() {
        return podId;
    }

    public void setPodId(final Long podId) {
        this.podId = podId;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(final long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    @Override
    public int getSentCount() {
        return sentCount;
    }

    public void setSentCount(final int sentCount) {
        this.sentCount = sentCount;
    }

    @Override
    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public Date getLastSent() {
        return lastSent;
    }

    public void setLastSent(final Date lastSent) {
        this.lastSent = lastSent;
    }

    @Override
    public Date getResolved() {
        return resolved;
    }

    public void setResolved(final Date resolved) {
        this.resolved = resolved;
    }

    @Override
    public boolean getArchived() {
        return archived;
    }

    public void setArchived(final Boolean archived) {
        this.archived = archived;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(final Long clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
