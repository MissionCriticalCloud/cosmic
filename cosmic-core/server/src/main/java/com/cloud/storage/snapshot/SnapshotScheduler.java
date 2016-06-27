package com.cloud.storage.snapshot;

import com.cloud.storage.SnapshotPolicyVO;
import com.cloud.utils.component.Manager;
import com.cloud.utils.concurrency.Scheduler;

import java.util.Date;

/**
 */
public interface SnapshotScheduler extends Manager, Scheduler {

    /**
     * Schedule the next snapshot job for this policy instance.
     *
     * @return The timestamp at which the next snapshot is scheduled.
     */
    public Date scheduleNextSnapshotJob(SnapshotPolicyVO policyInstance);

    /**
     * Remove schedule for volumeId, policyId combination
     *
     * @param volumeId
     * @param policyId
     * @return
     */
    boolean removeSchedule(Long volumeId, Long policyId);

    void scheduleOrCancelNextSnapshotJobOnDisplayChange(SnapshotPolicyVO policy, boolean previousDisplay);
}
