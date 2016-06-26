package org.apache.cloudstack.framework.jobs.impl;

import com.cloud.utils.DateUtil;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.jobs.JobInfo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "async_job_join_map")
public class AsyncJobJoinMapVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id = null;

    @Column(name = "job_id")
    private long jobId;

    @Column(name = "join_job_id")
    private long joinJobId;

    @Column(name = "join_status")
    @Enumerated(EnumType.ORDINAL)
    private JobInfo.Status joinStatus;

    @Column(name = "join_result", length = 1024)
    private String joinResult;

    @Column(name = "join_msid")
    private long joinMsid;

    @Column(name = "complete_msid")
    private Long completeMsid;

    @Column(name = "sync_source_id")
    private Long syncSourceId;

    @Column(name = "wakeup_handler")
    private String wakeupHandler;

    @Column(name = "wakeup_dispatcher")
    private String wakeupDispatcher;

    @Column(name = "wakeup_interval")
    private long wakeupInterval;

    @Column(name = GenericDao.CREATED_COLUMN)
    private Date created;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "next_wakeup")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextWakeupTime;

    @Column(name = "expiration")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiration;

    public AsyncJobJoinMapVO() {
        created = DateUtil.currentGMTTime();
        lastUpdated = DateUtil.currentGMTTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(final long jobId) {
        this.jobId = jobId;
    }

    public long getJoinJobId() {
        return joinJobId;
    }

    public void setJoinJobId(final long joinJobId) {
        this.joinJobId = joinJobId;
    }

    public JobInfo.Status getJoinStatus() {
        return joinStatus;
    }

    public void setJoinStatus(final JobInfo.Status joinStatus) {
        this.joinStatus = joinStatus;
    }

    public String getJoinResult() {
        return joinResult;
    }

    public void setJoinResult(final String joinResult) {
        this.joinResult = joinResult;
    }

    public long getJoinMsid() {
        return joinMsid;
    }

    public void setJoinMsid(final long joinMsid) {
        this.joinMsid = joinMsid;
    }

    public Long getCompleteMsid() {
        return completeMsid;
    }

    public void setCompleteMsid(final Long completeMsid) {
        this.completeMsid = completeMsid;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(final Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getSyncSourceId() {
        return syncSourceId;
    }

    public void setSyncSourceId(final Long syncSourceId) {
        this.syncSourceId = syncSourceId;
    }

    public String getWakeupHandler() {
        return wakeupHandler;
    }

    public void setWakeupHandler(final String wakeupHandler) {
        this.wakeupHandler = wakeupHandler;
    }

    public String getWakeupDispatcher() {
        return wakeupDispatcher;
    }

    public void setWakeupDispatcher(final String wakeupDispatcher) {
        this.wakeupDispatcher = wakeupDispatcher;
    }

    public long getWakeupInterval() {
        return wakeupInterval;
    }

    public void setWakeupInterval(final long wakeupInterval) {
        this.wakeupInterval = wakeupInterval;
    }

    public Date getNextWakeupTime() {
        return nextWakeupTime;
    }

    public void setNextWakeupTime(final Date nextWakeupTime) {
        this.nextWakeupTime = nextWakeupTime;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(final Date expiration) {
        this.expiration = expiration;
    }
}
