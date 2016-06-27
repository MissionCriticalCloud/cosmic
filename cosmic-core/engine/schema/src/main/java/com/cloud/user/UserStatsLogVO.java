package com.cloud.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "op_user_stats_log")
public class UserStatsLogVO {
    @Id
    @Column(name = "user_stats_id")
    private long userStatsId;

    @Column(name = "net_bytes_received")
    private long netBytesReceived;

    @Column(name = "net_bytes_sent")
    private long netBytesSent;

    @Column(name = "current_bytes_received")
    private long currentBytesReceived;

    @Column(name = "current_bytes_sent")
    private long currentBytesSent;

    @Column(name = "agg_bytes_received")
    private long aggBytesReceived;

    @Column(name = "agg_bytes_sent")
    private long aggBytesSent;

    @Column(name = "updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date updatedTime;

    public UserStatsLogVO() {
    }

    public UserStatsLogVO(final long userStatsId, final long netBytesReceived, final long netBytesSent, final long currentBytesReceived, final long currentBytesSent, final long
            aggBytesReceived,
                          final long aggBytesSent, final Date updatedTime) {
        this.userStatsId = userStatsId;
        this.netBytesReceived = netBytesReceived;
        this.netBytesSent = netBytesSent;
        this.currentBytesReceived = currentBytesReceived;
        this.currentBytesSent = currentBytesSent;
        this.aggBytesReceived = aggBytesReceived;
        this.aggBytesSent = aggBytesSent;
        this.updatedTime = updatedTime;
    }

    public Long getUserStatsId() {
        return userStatsId;
    }

    public long getCurrentBytesReceived() {
        return currentBytesReceived;
    }

    public void setCurrentBytesReceived(final long currentBytesReceived) {
        this.currentBytesReceived = currentBytesReceived;
    }

    public long getCurrentBytesSent() {
        return currentBytesSent;
    }

    public void setCurrentBytesSent(final long currentBytesSent) {
        this.currentBytesSent = currentBytesSent;
    }

    public long getNetBytesReceived() {
        return netBytesReceived;
    }

    public void setNetBytesReceived(final long netBytesReceived) {
        this.netBytesReceived = netBytesReceived;
    }

    public long getNetBytesSent() {
        return netBytesSent;
    }

    public void setNetBytesSent(final long netBytesSent) {
        this.netBytesSent = netBytesSent;
    }

    public long getAggBytesReceived() {
        return aggBytesReceived;
    }

    public void setAggBytesReceived(final long aggBytesReceived) {
        this.aggBytesReceived = aggBytesReceived;
    }

    public long getAggBytesSent() {
        return aggBytesSent;
    }

    public void setAggBytesSent(final long aggBytesSent) {
        this.aggBytesSent = aggBytesSent;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(final Date updatedTime) {
        this.updatedTime = updatedTime;
    }
}
