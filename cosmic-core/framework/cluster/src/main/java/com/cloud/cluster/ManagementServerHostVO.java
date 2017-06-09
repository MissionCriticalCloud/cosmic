package com.cloud.cluster;

import com.cloud.utils.db.GenericDao;

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
@Table(name = "mshost")
public class ManagementServerHostVO implements ManagementServerHost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "msid", nullable = false)
    private long msid;

    @Column(name = "runid", nullable = false)
    private long runid;

    @Column(name = "name")
    private String name;

    @Column(name = "state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ManagementServerHost.State state;

    @Column(name = "version")
    private String version;

    @Column(name = "service_ip", nullable = false)
    private String serviceIP;

    @Column(name = "service_port", nullable = false)
    private int servicePort;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update")
    private Date lastUpdateTime;

    @Column(name = GenericDao.REMOVED_COLUMN)
    private Date removed;

    @Column(name = "alert_count", nullable = false)
    private int alertCount;

    public ManagementServerHostVO() {
    }

    public ManagementServerHostVO(final long msid, final long runid, final String serviceIP, final int servicePort, final Date updateTime) {
        this.msid = msid;
        this.runid = runid;
        this.serviceIP = serviceIP;
        this.servicePort = servicePort;
        lastUpdateTime = updateTime;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public long getMsid() {
        return msid;
    }

    public void setMsid(final long msid) {
        this.msid = msid;
    }

    @Override
    public ManagementServerHost.State getState() {
        return state;
    }

    public void setState(final ManagementServerHost.State state) {
        this.state = state;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public String getServiceIP() {
        return serviceIP;
    }

    public void setServiceIP(final String serviceIP) {
        this.serviceIP = serviceIP;
    }

    public long getRunid() {
        return runid;
    }

    public void setRunid(final long runid) {
        this.runid = runid;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(final int servicePort) {
        this.servicePort = servicePort;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(final Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removedTime) {
        removed = removedTime;
    }

    public void setAlertCount(final int count) {
        alertCount = count;
    }

    @Override
    public String toString() {
        return new StringBuilder("ManagementServer[").append("-").append(id).append("-").append(msid).append("-").append(state).append("-").append(name).append("]").toString();
    }
}
