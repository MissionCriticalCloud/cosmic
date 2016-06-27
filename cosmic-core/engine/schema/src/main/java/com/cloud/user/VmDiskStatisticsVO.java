package com.cloud.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vm_disk_statistics")
public class VmDiskStatisticsVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "data_center_id", updatable = false)
    private long dataCenterId;

    @Column(name = "account_id", updatable = false)
    private long accountId;

    @Column(name = "vm_id")
    private Long vmId;

    @Column(name = "volume_id")
    private Long volumeId;

    @Column(name = "net_io_read")
    private long netIORead;

    @Column(name = "net_io_write")
    private long netIOWrite;

    @Column(name = "current_io_read")
    private long currentIORead;

    @Column(name = "current_io_write")
    private long currentIOWrite;

    @Column(name = "agg_io_read")
    private long aggIORead;

    @Column(name = "agg_io_write")
    private long aggIOWrite;

    @Column(name = "net_bytes_read")
    private long netBytesRead;

    @Column(name = "net_bytes_write")
    private long netBytesWrite;

    @Column(name = "current_bytes_read")
    private long currentBytesRead;

    @Column(name = "current_bytes_write")
    private long currentBytesWrite;

    @Column(name = "agg_bytes_read")
    private long aggBytesRead;

    @Column(name = "agg_bytes_write")
    private long aggBytesWrite;

    protected VmDiskStatisticsVO() {
    }

    public VmDiskStatisticsVO(final long accountId, final long dcId, final Long vmId, final Long volumeId) {
        this.accountId = accountId;
        this.dataCenterId = dcId;
        this.vmId = vmId;
        this.volumeId = volumeId;
        this.netBytesRead = 0;
        this.netBytesWrite = 0;
        this.currentBytesRead = 0;
        this.currentBytesWrite = 0;
        this.netBytesRead = 0;
        this.netBytesWrite = 0;
        this.currentBytesRead = 0;
        this.currentBytesWrite = 0;
    }

    public long getAccountId() {
        return accountId;
    }

    public Long getId() {
        return id;
    }

    public long getDataCenterId() {
        return dataCenterId;
    }

    public Long getVmId() {
        return vmId;
    }

    public Long getVolumeId() {
        return volumeId;
    }

    public long getCurrentIORead() {
        return currentIORead;
    }

    public void setCurrentIORead(final long currentIORead) {
        this.currentIORead = currentIORead;
    }

    public long getCurrentIOWrite() {
        return currentIOWrite;
    }

    public void setCurrentIOWrite(final long currentIOWrite) {
        this.currentIOWrite = currentIOWrite;
    }

    public long getNetIORead() {
        return netIORead;
    }

    public void setNetIORead(final long netIORead) {
        this.netIORead = netIORead;
    }

    public long getNetIOWrite() {
        return netIOWrite;
    }

    public void setNetIOWrite(final long netIOWrite) {
        this.netIOWrite = netIOWrite;
    }

    public long getAggIORead() {
        return aggIORead;
    }

    public void setAggIORead(final long aggIORead) {
        this.aggIORead = aggIORead;
    }

    public long getAggIOWrite() {
        return aggIOWrite;
    }

    public void setAggIOWrite(final long aggIOWrite) {
        this.aggIOWrite = aggIOWrite;
    }

    public long getCurrentBytesRead() {
        return currentBytesRead;
    }

    public void setCurrentBytesRead(final long currentBytesRead) {
        this.currentBytesRead = currentBytesRead;
    }

    public long getCurrentBytesWrite() {
        return currentBytesWrite;
    }

    public void setCurrentBytesWrite(final long currentBytesWrite) {
        this.currentBytesWrite = currentBytesWrite;
    }

    public long getNetBytesRead() {
        return netBytesRead;
    }

    public void setNetBytesRead(final long netBytesRead) {
        this.netBytesRead = netBytesRead;
    }

    public long getNetBytesWrite() {
        return netBytesWrite;
    }

    public void setNetBytesWrite(final long netBytesWrite) {
        this.netBytesWrite = netBytesWrite;
    }

    public long getAggBytesRead() {
        return aggBytesRead;
    }

    public void setAggBytesRead(final long aggBytesRead) {
        this.aggBytesRead = aggBytesRead;
    }

    public long getAggBytesWrite() {
        return aggBytesWrite;
    }

    public void setAggBytesWrite(final long aggBytesWrite) {
        this.aggBytesWrite = aggBytesWrite;
    }
}
