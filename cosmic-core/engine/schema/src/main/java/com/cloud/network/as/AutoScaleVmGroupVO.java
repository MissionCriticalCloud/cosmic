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
@Table(name = "autoscale_vmgroups")
@Inheritance(strategy = InheritanceType.JOINED)
public class AutoScaleVmGroupVO implements AutoScaleVmGroup, InternalIdentity {

    @Column(name = GenericDao.REMOVED_COLUMN)
    protected Date removed;
    @Column(name = GenericDao.CREATED_COLUMN)
    protected Date created;
    @Column(name = "display", updatable = true, nullable = false)
    protected boolean display = true;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "uuid")
    String uuid;
    @Column(name = "zone_id", updatable = false)
    private long zoneId;
    @Column(name = "domain_id", updatable = false)
    private long domainId;
    @Column(name = "account_id")
    private long accountId;
    @Column(name = "load_balancer_id")
    private Long loadBalancerId;
    @Column(name = "min_members", updatable = true)
    private int minMembers;
    @Column(name = "max_members", updatable = true)
    private int maxMembers;
    @Column(name = "member_port")
    private int memberPort;
    @Column(name = "interval")
    private int interval;
    @Column(name = "last_interval", updatable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastInterval;
    @Column(name = "profile_id")
    private long profileId;
    @Column(name = "state")
    private String state;

    public AutoScaleVmGroupVO() {
    }

    public AutoScaleVmGroupVO(final long lbRuleId, final long zoneId, final long domainId,
                              final long accountId, final int minMembers, final int maxMembers, final int memberPort,
                              final int interval, final Date lastInterval, final long profileId, final String state) {

        uuid = UUID.randomUUID().toString();
        loadBalancerId = lbRuleId;
        this.minMembers = minMembers;
        this.maxMembers = maxMembers;
        this.memberPort = memberPort;
        this.profileId = profileId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.zoneId = zoneId;
        this.state = state;
        this.interval = interval;
        this.lastInterval = lastInterval;
    }

    @Override
    public String toString() {
        return new StringBuilder("AutoScaleVmGroupVO[").append("id").append("]").toString();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    @Override
    public Long getLoadBalancerId() {
        return loadBalancerId;
    }

    @Override
    public long getProfileId() {
        return profileId;
    }

    @Override
    public int getMinMembers() {
        return minMembers;
    }

    @Override
    public int getMaxMembers() {
        return maxMembers;
    }

    @Override
    public int getMemberPort() {
        return memberPort;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public Date getLastInterval() {
        return lastInterval;
    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }

    public void setLastInterval(final Date lastInterval) {
        this.lastInterval = lastInterval;
    }

    public void setInterval(final Integer interval) {
        this.interval = interval;
    }

    public void setMaxMembers(final int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public void setMinMembers(final int minMembers) {
        this.minMembers = minMembers;
    }

    public void setLoadBalancerId(final Long loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public long getZoneId() {
        return zoneId;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public Date getRemoved() {
        return removed;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public Class<?> getEntityType() {
        return AutoScaleVmGroup.class;
    }
}
