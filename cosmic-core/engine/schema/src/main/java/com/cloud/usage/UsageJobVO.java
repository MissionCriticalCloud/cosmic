package com.cloud.usage;

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
@Table(name = "usage_job")
public class UsageJobVO implements InternalIdentity {

    public static final int JOB_TYPE_RECURRING = 0;
    public static final int JOB_TYPE_SINGLE = 1;

    public static final int JOB_NOT_SCHEDULED = 0;
    public static final int JOB_SCHEDULED = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "host")
    private String host;

    @Column(name = "pid")
    private Integer pid;

    @Column(name = "job_type")
    private int jobType;

    @Column(name = "scheduled")
    private int scheduled;

    @Column(name = "start_millis")
    private long startMillis;

    @Column(name = "end_millis")
    private long endMillis;

    @Column(name = "exec_time")
    private long execTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_date")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "success")
    private Boolean success;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "heartbeat")
    private Date heartbeat;

    public UsageJobVO() {
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(final Integer pid) {
        this.pid = pid;
    }

    public int getJobType() {
        return jobType;
    }

    public void setJobType(final int jobType) {
        this.jobType = jobType;
    }

    public int getScheduled() {
        return scheduled;
    }

    public void setScheduled(final int scheduled) {
        this.scheduled = scheduled;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public void setStartMillis(final long startMillis) {
        this.startMillis = startMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    public void setEndMillis(final long endMillis) {
        this.endMillis = endMillis;
    }

    public long getExecTime() {
        return execTime;
    }

    public void setExecTime(final long execTime) {
        this.execTime = execTime;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(final Boolean success) {
        this.success = success;
    }

    public Date getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(final Date heartbeat) {
        this.heartbeat = heartbeat;
    }
}
