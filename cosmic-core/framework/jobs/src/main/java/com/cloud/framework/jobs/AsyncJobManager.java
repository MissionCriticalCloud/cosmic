package com.cloud.framework.jobs;

import com.cloud.framework.jobs.impl.AsyncJobVO;
import com.cloud.jobs.JobInfo;
import com.cloud.utils.Predicate;
import com.cloud.utils.component.Manager;

import java.io.Serializable;
import java.util.List;

public interface AsyncJobManager extends Manager {

    String API_JOB_POOL_THREAD_PREFIX = "API-Job-Executor";
    String WORK_JOB_POOL_THREAD_PREFIX = "Work-Job-Executor";

    AsyncJobVO getAsyncJob(long jobId);

    List<? extends AsyncJob> findInstancePendingAsyncJobs(String instanceType, Long accountId);

    long submitAsyncJob(AsyncJob job);

    long submitAsyncJob(AsyncJob job, String syncObjType, long syncObjId);

    void completeAsyncJob(long jobId, JobInfo.Status jobStatus, int resultCode, String result);

    void updateAsyncJobStatus(long jobId, int processStatus, String resultObject);

    void updateAsyncJobAttachment(long jobId, String instanceType, Long instanceId);

    /**
     * A running thread inside management server can have a 1:1 linked pseudo job.
     * This is to help make some legacy code work without too dramatic changes.
     * <p>
     * All pseudo jobs should be expunged upon management start event
     *
     * @return pseudo job for the thread
     */
    AsyncJob getPseudoJob(long accountId, long userId);

    /**
     * Used by upper level job to wait for completion of a down-level job (usually VmWork jobs)
     * in synchronous way. Caller needs to use waitAndCheck() to check the completion status
     * of the down-level job
     * <p>
     * Due to the amount of legacy code that relies on synchronous-call semantics, this form of joinJob
     * is used mostly
     *
     * @param jobId     upper job that is going to wait the completion of a down-level job
     * @param joinJobId down-level job
     */
    void joinJob(long jobId, long joinJobId);

    /**
     * Dis-join two related jobs
     *
     * @param jobId
     * @param joinedJobId
     */
    void disjoinJob(long jobId, long joinedJobId);

    void syncAsyncJobExecution(AsyncJob job, String syncObjType, long syncObjId, long queueSizeLimit);

    /**
     * This method will be deprecated after all code has been migrated to fully-asynchronous mode
     * that uses async-feature of joinJob/disjoinJob
     *
     * @param wakupTopicsOnMessageBus     topic on message bus to wakeup the wait
     * @param checkIntervalInMilliSeconds time to break out wait for checking predicate condition
     * @param timeoutInMiliseconds        time out to break out the whole wait process
     * @param predicate
     * @return true, predicate condition is satisfied
     * false, wait is timed out
     */
    boolean waitAndCheck(AsyncJob job, String[] wakupTopicsOnMessageBus, long checkIntervalInMilliSeconds, long timeoutInMiliseconds, Predicate predicate);

    AsyncJob queryJob(long jobId, boolean updatePollTime);

    String marshallResultObject(Serializable obj);

    Object unmarshallResultObject(AsyncJob job);

    List<AsyncJobVO> findFailureAsyncJobs(String... cmds);
}
