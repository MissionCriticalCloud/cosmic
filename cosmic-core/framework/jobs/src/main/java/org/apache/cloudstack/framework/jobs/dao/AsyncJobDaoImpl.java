package org.apache.cloudstack.framework.jobs.dao;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import org.apache.cloudstack.framework.jobs.impl.AsyncJobVO;
import org.apache.cloudstack.jobs.JobInfo;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncJobDaoImpl extends GenericDaoBase<AsyncJobVO, Long> implements AsyncJobDao {
    private static final Logger s_logger = LoggerFactory.getLogger(AsyncJobDaoImpl.class.getName());

    private final SearchBuilder<AsyncJobVO> pendingAsyncJobSearch;
    private final SearchBuilder<AsyncJobVO> pendingAsyncJobsSearch;
    private final SearchBuilder<AsyncJobVO> expiringAsyncJobSearch;
    private final SearchBuilder<AsyncJobVO> pseudoJobSearch;
    private final SearchBuilder<AsyncJobVO> pseudoJobCleanupSearch;
    private final SearchBuilder<AsyncJobVO> expiringUnfinishedAsyncJobSearch;
    private final SearchBuilder<AsyncJobVO> expiringCompletedAsyncJobSearch;
    private final SearchBuilder<AsyncJobVO> failureMsidAsyncJobSearch;

    public AsyncJobDaoImpl() {
        pendingAsyncJobSearch = createSearchBuilder();
        pendingAsyncJobSearch.and("instanceType", pendingAsyncJobSearch.entity().getInstanceType(), SearchCriteria.Op.EQ);
        pendingAsyncJobSearch.and("instanceId", pendingAsyncJobSearch.entity().getInstanceId(), SearchCriteria.Op.EQ);
        pendingAsyncJobSearch.and("status", pendingAsyncJobSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        pendingAsyncJobSearch.done();

        expiringAsyncJobSearch = createSearchBuilder();
        expiringAsyncJobSearch.and("created", expiringAsyncJobSearch.entity().getCreated(), SearchCriteria.Op.LTEQ);
        expiringAsyncJobSearch.done();

        pendingAsyncJobsSearch = createSearchBuilder();
        pendingAsyncJobsSearch.and("instanceType", pendingAsyncJobsSearch.entity().getInstanceType(), SearchCriteria.Op.EQ);
        pendingAsyncJobsSearch.and("accountId", pendingAsyncJobsSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        pendingAsyncJobsSearch.and("status", pendingAsyncJobsSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        pendingAsyncJobsSearch.done();

        expiringUnfinishedAsyncJobSearch = createSearchBuilder();
        expiringUnfinishedAsyncJobSearch.and("jobDispatcher", expiringUnfinishedAsyncJobSearch.entity().getDispatcher(), SearchCriteria.Op.NEQ);
        expiringUnfinishedAsyncJobSearch.and("created", expiringUnfinishedAsyncJobSearch.entity().getCreated(), SearchCriteria.Op.LTEQ);
        expiringUnfinishedAsyncJobSearch.and("completeMsId", expiringUnfinishedAsyncJobSearch.entity().getCompleteMsid(), SearchCriteria.Op.NULL);
        expiringUnfinishedAsyncJobSearch.and("jobStatus", expiringUnfinishedAsyncJobSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        expiringUnfinishedAsyncJobSearch.done();

        expiringCompletedAsyncJobSearch = createSearchBuilder();
        expiringCompletedAsyncJobSearch.and("created", expiringCompletedAsyncJobSearch.entity().getCreated(), SearchCriteria.Op.LTEQ);
        expiringCompletedAsyncJobSearch.and("completeMsId", expiringCompletedAsyncJobSearch.entity().getCompleteMsid(), SearchCriteria.Op.NNULL);
        expiringCompletedAsyncJobSearch.and("jobStatus", expiringCompletedAsyncJobSearch.entity().getStatus(), SearchCriteria.Op.NEQ);
        expiringCompletedAsyncJobSearch.done();

        pseudoJobSearch = createSearchBuilder();
        pseudoJobSearch.and("jobDispatcher", pseudoJobSearch.entity().getDispatcher(), Op.EQ);
        pseudoJobSearch.and("instanceType", pseudoJobSearch.entity().getInstanceType(), Op.EQ);
        pseudoJobSearch.and("instanceId", pseudoJobSearch.entity().getInstanceId(), Op.EQ);
        pseudoJobSearch.done();

        pseudoJobCleanupSearch = createSearchBuilder();
        pseudoJobCleanupSearch.and("initMsid", pseudoJobCleanupSearch.entity().getInitMsid(), Op.EQ);
        pseudoJobCleanupSearch.done();

        failureMsidAsyncJobSearch = createSearchBuilder();
        failureMsidAsyncJobSearch.and("initMsid", failureMsidAsyncJobSearch.entity().getInitMsid(), Op.EQ);
        failureMsidAsyncJobSearch.and("instanceType", failureMsidAsyncJobSearch.entity().getInstanceType(), SearchCriteria.Op.EQ);
        failureMsidAsyncJobSearch.and("status", failureMsidAsyncJobSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        failureMsidAsyncJobSearch.and("job_cmd", failureMsidAsyncJobSearch.entity().getCmd(), Op.IN);
        failureMsidAsyncJobSearch.done();
    }

    @Override
    public AsyncJobVO findInstancePendingAsyncJob(final String instanceType, final long instanceId) {
        final SearchCriteria<AsyncJobVO> sc = pendingAsyncJobSearch.create();
        sc.setParameters("instanceType", instanceType);
        sc.setParameters("instanceId", instanceId);
        sc.setParameters("status", JobInfo.Status.IN_PROGRESS);

        final List<AsyncJobVO> l = listIncludingRemovedBy(sc);
        if (l != null && l.size() > 0) {
            if (l.size() > 1) {
                s_logger.warn("Instance " + instanceType + "-" + instanceId + " has multiple pending async-job");
            }

            return l.get(0);
        }
        return null;
    }

    @Override
    public List<AsyncJobVO> findInstancePendingAsyncJobs(final String instanceType, final Long accountId) {
        final SearchCriteria<AsyncJobVO> sc = pendingAsyncJobsSearch.create();
        sc.setParameters("instanceType", instanceType);

        if (accountId != null) {
            sc.setParameters("accountId", accountId);
        }
        sc.setParameters("status", JobInfo.Status.IN_PROGRESS);

        return listBy(sc);
    }

    @Override
    public AsyncJobVO findPseudoJob(final long threadId, final long msid) {
        final SearchCriteria<AsyncJobVO> sc = pseudoJobSearch.create();
        sc.setParameters("jobDispatcher", AsyncJobVO.JOB_DISPATCHER_PSEUDO);
        sc.setParameters("instanceType", AsyncJobVO.PSEUDO_JOB_INSTANCE_TYPE);
        sc.setParameters("instanceId", threadId);

        final List<AsyncJobVO> result = listBy(sc);
        if (result != null && result.size() > 0) {
            assert (result.size() == 1);
            return result.get(0);
        }

        return null;
    }

    @Override
    public void cleanupPseduoJobs(final long msid) {
        final SearchCriteria<AsyncJobVO> sc = pseudoJobCleanupSearch.create();
        sc.setParameters("initMsid", msid);
        this.expunge(sc);
    }

    @Override
    public List<AsyncJobVO> getExpiredJobs(final Date cutTime, final int limit) {
        final SearchCriteria<AsyncJobVO> sc = expiringAsyncJobSearch.create();
        sc.setParameters("created", cutTime);
        final Filter filter = new Filter(AsyncJobVO.class, "created", true, 0L, (long) limit);
        return listIncludingRemovedBy(sc, filter);
    }

    @Override
    public List<AsyncJobVO> getExpiredUnfinishedJobs(final Date cutTime, final int limit) {
        final SearchCriteria<AsyncJobVO> sc = expiringUnfinishedAsyncJobSearch.create();
        sc.setParameters("jobDispatcher", AsyncJobVO.JOB_DISPATCHER_PSEUDO);
        sc.setParameters("created", cutTime);
        sc.setParameters("jobStatus", JobInfo.Status.IN_PROGRESS);
        final Filter filter = new Filter(AsyncJobVO.class, "created", true, 0L, (long) limit);
        return listIncludingRemovedBy(sc, filter);
    }

    @Override
    @DB
    public void resetJobProcess(final long msid, final int jobResultCode, final String jobResultMessage) {
        final String sql = "UPDATE async_job SET job_status=?, job_result_code=?, job_result=? where job_status=? AND (job_executing_msid=? OR (job_executing_msid IS NULL AND " +
                "job_init_msid=?))";
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setInt(1, JobInfo.Status.FAILED.ordinal());
            pstmt.setInt(2, jobResultCode);
            pstmt.setString(3, jobResultMessage);
            pstmt.setInt(4, JobInfo.Status.IN_PROGRESS.ordinal());
            pstmt.setLong(5, msid);
            pstmt.setLong(6, msid);
            pstmt.execute();
        } catch (final SQLException e) {
            s_logger.warn("Unable to reset job status for management server " + msid, e);
        } catch (final Throwable e) {
            s_logger.warn("Unable to reset job status for management server " + msid, e);
        }
    }

    @Override
    public List<AsyncJobVO> getExpiredCompletedJobs(final Date cutTime, final int limit) {
        final SearchCriteria<AsyncJobVO> sc = expiringCompletedAsyncJobSearch.create();
        sc.setParameters("created", cutTime);
        sc.setParameters("jobStatus", JobInfo.Status.IN_PROGRESS);
        final Filter filter = new Filter(AsyncJobVO.class, "created", true, 0L, (long) limit);
        return listIncludingRemovedBy(sc, filter);
    }

    @Override
    public List<AsyncJobVO> getResetJobs(final long msid) {
        final SearchCriteria<AsyncJobVO> sc = pendingAsyncJobSearch.create();
        sc.setParameters("status", JobInfo.Status.IN_PROGRESS);

        // construct query: (job_executing_msid=msid OR (job_executing_msid IS NULL AND job_init_msid=msid))
        final SearchCriteria<AsyncJobVO> msQuery = createSearchCriteria();
        msQuery.addOr("executingMsid", SearchCriteria.Op.EQ, msid);
        final SearchCriteria<AsyncJobVO> initMsQuery = createSearchCriteria();
        initMsQuery.addAnd("executingMsid", SearchCriteria.Op.NULL);
        initMsQuery.addAnd("initMsid", SearchCriteria.Op.EQ, msid);
        msQuery.addOr("initMsid", SearchCriteria.Op.SC, initMsQuery);

        sc.addAnd("executingMsid", SearchCriteria.Op.SC, msQuery);

        final Filter filter = new Filter(AsyncJobVO.class, "created", true, null, null);
        return listIncludingRemovedBy(sc, filter);
    }

    @Override
    public List<AsyncJobVO> getFailureJobsSinceLastMsStart(final long msId, final String... cmds) {
        final SearchCriteria<AsyncJobVO> sc = failureMsidAsyncJobSearch.create();
        sc.setParameters("initMsid", msId);
        sc.setParameters("status", AsyncJobVO.Status.FAILED);
        sc.setParameters("job_cmd", (Object[]) cmds);
        return listBy(sc);
    }
}
