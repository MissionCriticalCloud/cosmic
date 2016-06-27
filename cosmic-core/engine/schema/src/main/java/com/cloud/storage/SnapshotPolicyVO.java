package com.cloud.storage;

import com.cloud.storage.snapshot.SnapshotPolicy;
import com.cloud.utils.DateUtil.IntervalType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "snapshot_policy")
public class SnapshotPolicyVO implements SnapshotPolicy {

    @Column(name = "display", updatable = true, nullable = false)
    protected boolean display = true;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;
    @Column(name = "volume_id")
    long volumeId;
    @Column(name = "schedule")
    String schedule;
    @Column(name = "timezone")
    String timezone;
    @Column(name = "active")
    boolean active = false;
    @Column(name = "uuid")
    String uuid;
    @Column(name = "interval")
    private short interval;
    @Column(name = "max_snaps")
    private int maxSnaps;

    public SnapshotPolicyVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public SnapshotPolicyVO(final long volumeId, final String schedule, final String timezone, final IntervalType intvType, final int maxSnaps, final boolean display) {
        this.volumeId = volumeId;
        this.schedule = schedule;
        this.timezone = timezone;
        this.interval = (short) intvType.ordinal();
        this.maxSnaps = maxSnaps;
        this.active = true;
        this.display = display;
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getVolumeId() {
        return volumeId;
    }

    @Override
    public String getSchedule() {
        return schedule;
    }

    @Override
    public void setSchedule(final String schedule) {
        this.schedule = schedule;
    }

    @Override
    public String getTimezone() {
        return timezone;
    }

    @Override
    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    @Override
    public short getInterval() {
        return interval;
    }

    @Override
    public void setInterval(final short interval) {
        this.interval = interval;
    }

    @Override
    public int getMaxSnaps() {
        return maxSnaps;
    }

    @Override
    public void setMaxSnaps(final int maxSnaps) {
        this.maxSnaps = maxSnaps;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(final boolean active) {
        this.active = active;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(final boolean display) {
        this.display = display;
    }
}
