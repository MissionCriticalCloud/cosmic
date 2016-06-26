package com.cloud.storage.snapshot;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface SnapshotSchedule extends InternalIdentity, Identity {

    Long getVolumeId();

    Long getPolicyId();

    void setPolicyId(long policyId);

    /**
     * @return the scheduledTimestamp
     */
    Date getScheduledTimestamp();

    void setScheduledTimestamp(Date scheduledTimestamp);

    Long getAsyncJobId();

    void setAsyncJobId(Long asyncJobId);

    Long getSnapshotId();

    void setSnapshotId(Long snapshotId);
}
