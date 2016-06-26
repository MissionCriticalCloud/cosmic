package com.cloud.storage;

import com.cloud.storage.snapshot.SnapshotSchedule;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "snapshot_schedule")
public class SnapshotScheduleVO implements SnapshotSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    // DB constraint: For a given volume and policyId, there will only be one
    // entry in this table.
    @Column(name = "volume_id")
    long volumeId;

    @Column(name = "policy_id")
    long policyId;

    @Column(name = "scheduled_timestamp")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date scheduledTimestamp;

    @Column(name = "async_job_id")
    Long asyncJobId;

    @Column(name = "snapshot_id")
    Long snapshotId;

    @Column(name = "uuid")
    String uuid = UUID.randomUUID().toString();

    public SnapshotScheduleVO() {
    }

    public SnapshotScheduleVO(final long volumeId, final long policyId, final Date scheduledTimestamp) {
        this.volumeId = volumeId;
        this.policyId = policyId;
        this.scheduledTimestamp = scheduledTimestamp;
        this.snapshotId = null;
        this.asyncJobId = null;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Long getVolumeId() {
        return volumeId;
    }

    @Override
    public Long getPolicyId() {
        return policyId;
    }

    @Override
    public void setPolicyId(final long policyId) {
        this.policyId = policyId;
    }

    /**
     * @return the scheduledTimestamp
     */
    @Override
    public Date getScheduledTimestamp() {
        return scheduledTimestamp;
    }

    @Override
    public void setScheduledTimestamp(final Date scheduledTimestamp) {
        this.scheduledTimestamp = scheduledTimestamp;
    }

    @Override
    public Long getAsyncJobId() {
        return asyncJobId;
    }

    @Override
    public void setAsyncJobId(final Long asyncJobId) {
        this.asyncJobId = asyncJobId;
    }

    @Override
    public Long getSnapshotId() {
        return snapshotId;
    }

    @Override
    public void setSnapshotId(final Long snapshotId) {
        this.snapshotId = snapshotId;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
