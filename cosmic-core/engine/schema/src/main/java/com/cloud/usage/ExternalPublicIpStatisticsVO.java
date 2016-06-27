package com.cloud.usage;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "external_public_ip_statistics")
@PrimaryKeyJoinColumn(name = "id")
public class ExternalPublicIpStatisticsVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "data_center_id", updatable = false)
    private long zoneId;

    @Column(name = "account_id", updatable = false)
    private long accountId;

    @Column(name = "public_ip_address")
    private String publicIpAddress;

    @Column(name = "current_bytes_received")
    private long currentBytesReceived;

    @Column(name = "current_bytes_sent")
    private long currentBytesSent;

    protected ExternalPublicIpStatisticsVO() {
    }

    public ExternalPublicIpStatisticsVO(final long zoneId, final long accountId, final String publicIpAddress) {
        this.zoneId = zoneId;
        this.accountId = accountId;
        this.publicIpAddress = publicIpAddress;
        this.currentBytesReceived = 0;
        this.currentBytesSent = 0;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getZoneId() {
        return zoneId;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getPublicIpAddress() {
        return publicIpAddress;
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
}
