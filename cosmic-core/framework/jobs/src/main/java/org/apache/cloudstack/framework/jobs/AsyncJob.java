package org.apache.cloudstack.framework.jobs;

import org.apache.cloudstack.framework.jobs.impl.SyncQueueItem;
import org.apache.cloudstack.jobs.JobInfo;

import java.util.Date;

public interface AsyncJob extends JobInfo {

    @Override
    String getType();

    @Override
    String getDispatcher();

    @Override
    int getPendingSignals();

    @Override
    long getUserId();

    @Override
    long getAccountId();

    @Override
    String getCmd();

    @Override
    int getCmdVersion();

    @Override
    String getCmdInfo();

    @Override
    Status getStatus();

    @Override
    int getProcessStatus();

    @Override
    int getResultCode();

    @Override
    String getResult();

    @Override
    Long getInitMsid();

    void setInitMsid(Long msid);

    @Override
    Long getExecutingMsid();

    @Override
    Long getCompleteMsid();

    void setCompleteMsid(Long msid);

    @Override
    Date getCreated();

    @Override
    Date getLastUpdated();

    @Override
    Date getLastPolled();

    @Override
    String getInstanceType();

    @Override
    Long getInstanceId();

    String getShortUuid();

    SyncQueueItem getSyncSource();

    void setSyncSource(SyncQueueItem item);

    String getRelated();

    public enum JournalType {
        SUCCESS, FAILURE
    }

    public static interface Topics {
        public static final String JOB_HEARTBEAT = "job.heartbeat";
        public static final String JOB_STATE = "job.state";
        public static final String JOB_EVENT_PUBLISH = "job.eventpublish";
    }

    public static interface Constants {

        // Although we may have detailed masks for each individual wakeup event, i.e.
        // periodical timer, matched topic from message bus, it seems that we don't
        // need to distinguish them to such level. Therefore, only one wakeup signal
        // is defined
        public static final int SIGNAL_MASK_WAKEUP = 1;

        public static final String SYNC_LOCK_NAME = "SyncLock";
    }
}
