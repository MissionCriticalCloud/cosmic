package com.cloud.user;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_statistics")
public class UserStatisticsVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "data_center_id", updatable = false)
    private long dataCenterId;

    @Column(name = "account_id", updatable = false)
    private long accountId;

    @Column(name = "public_ip_address")
    private String publicIpAddress;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "network_id")
    private Long networkId;

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

    protected UserStatisticsVO() {
    }

    public UserStatisticsVO(final long accountId, final long dcId, final String publicIpAddress, final Long deviceId, final String deviceType, final Long networkId) {
        this.accountId = accountId;
        this.dataCenterId = dcId;
        this.publicIpAddress = publicIpAddress;
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.networkId = networkId;
        this.netBytesReceived = 0;
        this.netBytesSent = 0;
        this.currentBytesReceived = 0;
        this.currentBytesSent = 0;
    }

    public long getAccountId() {
        return accountId;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public Long getNetworkId() {
        return networkId;
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
}
