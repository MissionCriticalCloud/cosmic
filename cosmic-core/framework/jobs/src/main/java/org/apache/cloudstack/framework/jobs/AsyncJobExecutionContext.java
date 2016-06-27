package org.apache.cloudstack.framework.jobs;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import com.cloud.user.User;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.jobs.dao.AsyncJobJoinMapDao;
import org.apache.cloudstack.framework.jobs.impl.AsyncJobJoinMapVO;
import org.apache.cloudstack.framework.jobs.impl.JobSerializerHelper;
import org.apache.cloudstack.framework.jobs.impl.SyncQueueItem;
import org.apache.cloudstack.jobs.JobInfo;
import org.apache.cloudstack.managed.threadlocal.ManagedThreadLocal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncJobExecutionContext {
    private static final Logger s_logger = LoggerFactory.getLogger(AsyncJobExecutionContext.class);
    static private AsyncJobManager s_jobMgr;
    static private AsyncJobJoinMapDao s_joinMapDao;
    private static final ManagedThreadLocal<AsyncJobExecutionContext> s_currentExectionContext = new ManagedThreadLocal<>();
    private AsyncJob _job;

    public AsyncJobExecutionContext() {
    }

    public AsyncJobExecutionContext(final AsyncJob job) {
        _job = job;
    }

    public static void init(final AsyncJobManager jobMgr, final AsyncJobJoinMapDao joinMapDao) {
        s_jobMgr = jobMgr;
        s_joinMapDao = joinMapDao;
    }

    // return currentExecutionContext without create it
    public static AsyncJobExecutionContext getCurrent() {
        return s_currentExectionContext.get();
    }

    public static AsyncJobExecutionContext unregister() {
        final AsyncJobExecutionContext context = s_currentExectionContext.get();
        setCurrentExecutionContext(null);
        return context;
    }

    public static String getOriginJobId() {
        final AsyncJobExecutionContext context = AsyncJobExecutionContext.getCurrentExecutionContext();
        if (context != null && context.getJob() != null) {
            return "" + context.getJob().getId();
        }

        return "";
    }

    public static AsyncJobExecutionContext getCurrentExecutionContext() {
        AsyncJobExecutionContext context = s_currentExectionContext.get();
        if (context == null) {
            // TODO, this has security implications, operations carried from API layer should always
            // set its context, otherwise, the fall-back here will use system security context
            //
            s_logger.warn("Job is executed without a context, setup psudo job for the executing thread");
            if (CallContext.current() != null) {
                context = registerPseudoExecutionContext(CallContext.current().getCallingAccountId(),
                        CallContext.current().getCallingUserId());
            } else {
                context = registerPseudoExecutionContext(Account.ACCOUNT_ID_SYSTEM, User.UID_SYSTEM);
            }
        }
        return context;
    }

    public AsyncJob getJob() {
        return _job;
    }

    public void setJob(final AsyncJob job) {
        _job = job;
    }

    public static AsyncJobExecutionContext registerPseudoExecutionContext(final long accountId, final long userId) {
        AsyncJobExecutionContext context = s_currentExectionContext.get();
        if (context == null) {
            context = new AsyncJobExecutionContext();
            context.setJob(s_jobMgr.getPseudoJob(accountId, userId));
            setCurrentExecutionContext(context);
        }

        return context;
    }

    // This is intended to be package level access for AsyncJobManagerImpl only.
    public static void setCurrentExecutionContext(final AsyncJobExecutionContext currentContext) {
        s_currentExectionContext.set(currentContext);
    }

    public SyncQueueItem getSyncSource() {
        return _job.getSyncSource();
    }

    public void resetSyncSource() {
        _job.setSyncSource(null);
    }

    public boolean isJobDispatchedBy(final String jobDispatcherName) {
        assert (jobDispatcherName != null);
        if (_job != null && _job.getDispatcher() != null && _job.getDispatcher().equals(jobDispatcherName)) {
            return true;
        }

        return false;
    }

    public void completeAsyncJob(final JobInfo.Status jobStatus, final int resultCode, final String resultObject) {
        assert (_job != null);
        s_jobMgr.completeAsyncJob(_job.getId(), jobStatus, resultCode, resultObject);
    }

    public void updateAsyncJobStatus(final int processStatus, final String resultObject) {
        assert (_job != null);
        s_jobMgr.updateAsyncJobStatus(_job.getId(), processStatus, resultObject);
    }

    public void updateAsyncJobAttachment(final String instanceType, final Long instanceId) {
        assert (_job != null);
        s_jobMgr.updateAsyncJobAttachment(_job.getId(), instanceType, instanceId);
    }

    public void logJobJournal(final AsyncJob.JournalType journalType, final String journalText, final String journalObjJson) {
        assert (_job != null);
        s_jobMgr.logJobJournal(_job.getId(), journalType, journalText, journalObjJson);
    }

    public void log(final Logger logger, final String journalText) {
        s_jobMgr.logJobJournal(_job.getId(), AsyncJob.JournalType.SUCCESS, journalText, null);
        logger.debug(journalText);
    }

    public void joinJob(final long joinJobId) {
        assert (_job != null);
        s_jobMgr.joinJob(_job.getId(), joinJobId);
    }

    public void joinJob(final long joinJobId, final String wakeupHandler, final String wakeupDispatcher,
                        final String[] wakeupTopcisOnMessageBus, final long wakeupIntervalInMilliSeconds, final long timeoutInMilliSeconds) {
        assert (_job != null);
        s_jobMgr.joinJob(_job.getId(), joinJobId, wakeupHandler, wakeupDispatcher, wakeupTopcisOnMessageBus,
                wakeupIntervalInMilliSeconds, timeoutInMilliSeconds);
    }

    //
    // check failure exception before we disjoin the worker job, work job usually fails with exception
    // this will help propogate exception between jobs
    // TODO : it is ugly and this will become unnecessary after we switch to full-async mode
    //
    public void disjoinJob(final long joinedJobId) throws InsufficientCapacityException,
            ConcurrentOperationException, ResourceUnavailableException {
        assert (_job != null);

        final AsyncJobJoinMapVO record = s_joinMapDao.getJoinRecord(_job.getId(), joinedJobId);
        s_jobMgr.disjoinJob(_job.getId(), joinedJobId);

        if (record.getJoinStatus() == JobInfo.Status.FAILED) {
            if (record.getJoinResult() != null) {
                final Object exception = JobSerializerHelper.fromObjectSerializedString(record.getJoinResult());
                if (exception != null && exception instanceof Exception) {
                    if (exception instanceof InsufficientCapacityException) {
                        s_logger.error("Job " + joinedJobId + " failed with InsufficientCapacityException");
                        throw (InsufficientCapacityException) exception;
                    } else if (exception instanceof ConcurrentOperationException) {
                        s_logger.error("Job " + joinedJobId + " failed with ConcurrentOperationException");
                        throw (ConcurrentOperationException) exception;
                    } else if (exception instanceof ResourceUnavailableException) {
                        s_logger.error("Job " + joinedJobId + " failed with ResourceUnavailableException");
                        throw (ResourceUnavailableException) exception;
                    } else {
                        s_logger.error("Job " + joinedJobId + " failed with exception");
                        throw new RuntimeException((Exception) exception);
                    }
                }
            } else {
                s_logger.error("Job " + joinedJobId + " failed without providing an error object");
                throw new RuntimeException("Job " + joinedJobId + " failed without providing an error object");
            }
        }
    }

    public void completeJoin(final JobInfo.Status joinStatus, final String joinResult) {
        assert (_job != null);
        s_jobMgr.completeJoin(_job.getId(), joinStatus, joinResult);
    }

    public void completeJobAndJoin(final JobInfo.Status joinStatus, final String joinResult) {
        assert (_job != null);
        s_jobMgr.completeJoin(_job.getId(), joinStatus, joinResult);
        s_jobMgr.completeAsyncJob(_job.getId(), joinStatus, 0, null);
    }
}
