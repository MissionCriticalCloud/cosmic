package com.cloud.usage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "usage_vm_disk")
public class UsageVmDiskVO {
    @Id
    @Column(name = "account_id")
    private long accountId;

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "vm_id")
    private Long vmId;

    @Column(name = "volume_id")
    private Long volumeId;

    @Column(name = "io_read")
    private long ioRead;

    @Column(name = "io_write")
    private long ioWrite;

    @Column(name = "agg_io_write")
    private long aggIOWrite;

    @Column(name = "agg_io_read")
    private long aggIORead;

    @Column(name = "bytes_read")
    private long bytesRead;

    @Column(name = "bytes_write")
    private long bytesWrite;

    @Column(name = "agg_bytes_write")
    private long aggBytesWrite;

    @Column(name = "agg_bytes_read")
    private long aggBytesRead;

    @Column(name = "event_time_millis")
    private long eventTimeMillis = 0;

    protected UsageVmDiskVO() {
    }

    public UsageVmDiskVO(final Long accountId, final long zoneId, final Long vmId, final Long volumeId, final long ioRead, final long ioWrite, final long aggIORead, final long
            aggIOWrite, final long bytesRead,
                         final long bytesWrite, final long aggBytesRead, final long aggBytesWrite, final long eventTimeMillis) {
        this.accountId = accountId;
        this.zoneId = zoneId;
        this.vmId = vmId;
        this.volumeId = volumeId;
        this.ioRead = ioRead;
        this.ioWrite = ioWrite;
        this.aggIOWrite = aggIOWrite;
        this.aggIORead = aggIORead;
        this.bytesRead = bytesRead;
        this.bytesWrite = bytesWrite;
        this.aggBytesWrite = aggBytesWrite;
        this.aggBytesRead = aggBytesRead;
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

    public Long getIORead() {
        return ioRead;
    }

    public void setIORead(final Long ioRead) {
        this.ioRead = ioRead;
    }

    public Long getIOWrite() {
        return ioWrite;
    }

    public void setIOWrite(final Long ioWrite) {
        this.ioWrite = ioWrite;
    }

    public Long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(final Long bytesRead) {
        this.bytesRead = bytesRead;
    }

    public Long getBytesWrite() {
        return bytesWrite;
    }

    public void setBytesWrite(final Long bytesWrite) {
        this.bytesWrite = bytesWrite;
    }

    public long getEventTimeMillis() {
        return eventTimeMillis;
    }

    public void setEventTimeMillis(final long eventTimeMillis) {
        this.eventTimeMillis = eventTimeMillis;
    }

    public Long getVmId() {
        return vmId;
    }

    public Long getVolumeId() {
        return volumeId;
    }

    public long getAggIOWrite() {
        return aggIOWrite;
    }

    public void setAggIOWrite(final long aggIOWrite) {
        this.aggIOWrite = aggIOWrite;
    }

    public long getAggIORead() {
        return aggIORead;
    }

    public void setAggIORead(final long aggIORead) {
        this.aggIORead = aggIORead;
    }

    public long getAggBytesWrite() {
        return aggBytesWrite;
    }

    public void setAggBytesWrite(final long aggBytesWrite) {
        this.aggBytesWrite = aggBytesWrite;
    }

    public long getAggBytesRead() {
        return aggBytesRead;
    }

    public void setAggBytesRead(final long aggBytesRead) {
        this.aggBytesRead = aggBytesRead;
    }
}
