package com.cloud.network.as;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "autoscale_policies")
@Inheritance(strategy = InheritanceType.JOINED)
public class AutoScalePolicyVO implements AutoScalePolicy, InternalIdentity {

    @Column(name = GenericDao.REMOVED_COLUMN)
    protected Date removed;
    @Column(name = GenericDao.CREATED_COLUMN)
    protected Date created;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "uuid")
    String uuid;
    @Column(name = "domain_id")
    private long domainId;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "duration")
    private int duration;
    @Column(name = "quiet_time", updatable = true, nullable = false)
    private int quietTime;
    @Column(name = "last_quiet_time", updatable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastQuiteTime;
    @Column(name = "action", updatable = false, nullable = false)
    private String action;

    public AutoScalePolicyVO() {
    }

    public AutoScalePolicyVO(final long domainId, final long accountId, final int duration,
                             final int quietTime, final Date lastQuiteTime, final String action) {
        uuid = UUID.randomUUID().toString();
        this.domainId = domainId;
        this.accountId = accountId;
        this.duration = duration;
        this.quietTime = quietTime;
        this.lastQuiteTime = lastQuiteTime;
        this.action = action;
    }

    @Override
    public String toString() {
        return new StringBuilder("AutoScalePolicy[").append("id-").append(id).append("]").toString();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public int getQuietTime() {
        return quietTime;
    }

    @Override
    public Date getLastQuiteTime() {
        return lastQuiteTime;
    }

    @Override
    public String getAction() {
        return action;
    }

    public void setLastQuiteTime(final Date lastQuiteTime) {
        this.lastQuiteTime = lastQuiteTime;
    }

    public void setQuietTime(final Integer quietTime) {
        this.quietTime = quietTime;
    }

    public void setDuration(final Integer duration) {
        this.duration = duration;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public Date getRemoved() {
        return removed;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public Class<?> getEntityType() {
        return AutoScalePolicy.class;
    }
}
