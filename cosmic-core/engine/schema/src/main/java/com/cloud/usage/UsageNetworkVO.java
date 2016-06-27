package com.cloud.usage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "usage_network")
public class UsageNetworkVO {
    @Id
    @Column(name = "account_id")
    private long accountId;

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "host_id")
    private long hostId;

    @Column(name = "host_type")
    private String hostType;

    @Column(name = "network_id")
    private Long networkId;

    @Column(name = "bytes_sent")
    private long bytesSent;

    @Column(name = "bytes_received")
    private long bytesReceived;

    @Column(name = "agg_bytes_received")
    private long aggBytesReceived;

    @Column(name = "agg_bytes_sent")
    private long aggBytesSent;

    @Column(name = "event_time_millis")
    private long eventTimeMillis = 0;

    protected UsageNetworkVO() {
    }

    public UsageNetworkVO(final Long accountId, final long zoneId, final long hostId, final String hostType, final Long networkId, final long bytesSent, final long
            bytesReceived, final long aggBytesReceived,
                          final long aggBytesSent, final long eventTimeMillis) {
        this.accountId = accountId;
        this.zoneId = zoneId;
        this.hostId = hostId;
        this.hostType = hostType;
        this.networkId = networkId;
        this.bytesSent = bytesSent;
        this.bytesReceived = bytesReceived;
        this.aggBytesReceived = aggBytesReceived;
        this.aggBytesSent = aggBytesSent;
        this.eventTimeMillis = eventTimeMillis;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }

    public long getZoneId() {
        return zoneId;
    }

    public void setZoneId(final long zoneId) {
        this.zoneId = zoneId;
    }

    public Long getBytesSent() {
        return bytesSent;
    }

    public void setBytesSent(final Long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public Long getBytesReceived() {
        return bytesReceived;
    }

    public void setBytes(final Long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public long getEventTimeMillis() {
        return eventTimeMillis;
    }

    public void setEventTimeMillis(final long eventTimeMillis) {
        this.eventTimeMillis = eventTimeMillis;
    }

    public long getHostId() {
        return hostId;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }

    public String getHostType() {
        return hostType;
    }

    public Long getNetworkId() {
        return networkId;
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
}
