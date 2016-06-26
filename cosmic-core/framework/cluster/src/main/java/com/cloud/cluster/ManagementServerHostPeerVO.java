package com.cloud.cluster;

import com.cloud.utils.DateUtil;

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
@Table(name = "mshost_peer")
public class ManagementServerHostPeerVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "owner_mshost", updatable = true, nullable = false)
    private long ownerMshost;

    @Column(name = "peer_mshost", updatable = true, nullable = false)
    private long peerMshost;

    @Column(name = "peer_runid", updatable = true, nullable = false)
    private long peerRunid;

    @Column(name = "peer_state", updatable = true, nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ManagementServerHost.State peerState;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update", updatable = true, nullable = true)
    private Date lastUpdateTime;

    public ManagementServerHostPeerVO() {
    }

    public ManagementServerHostPeerVO(final long ownerMshost, final long peerMshost, final long peerRunid, final ManagementServerHost.State peerState) {
        this.ownerMshost = ownerMshost;
        this.peerMshost = peerMshost;
        this.peerRunid = peerRunid;
        this.peerState = peerState;

        lastUpdateTime = DateUtil.currentGMTTime();
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getOwnerMshost() {
        return ownerMshost;
    }

    public void setOwnerMshost(final long ownerMshost) {
        this.ownerMshost = ownerMshost;
    }

    public long getPeerMshost() {
        return peerMshost;
    }

    public void setPeerMshost(final long peerMshost) {
        this.peerMshost = peerMshost;
    }

    public long getPeerRunid() {
        return peerRunid;
    }

    public void setPeerRunid(final long peerRunid) {
        this.peerRunid = peerRunid;
    }

    public ManagementServerHost.State getPeerState() {
        return peerState;
    }

    public void setPeerState(final ManagementServerHost.State peerState) {
        this.peerState = peerState;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(final Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
