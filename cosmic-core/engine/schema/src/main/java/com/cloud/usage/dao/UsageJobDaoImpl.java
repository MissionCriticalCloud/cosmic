package com.cloud.usage.dao;

import com.cloud.usage.UsageJobVO;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UsageJobDaoImpl extends GenericDaoBase<UsageJobVO, Long> implements UsageJobDao {
    private static final Logger s_logger = LoggerFactory.getLogger(UsageJobDaoImpl.class.getName());

    private static final String GET_LAST_JOB_SUCCESS_DATE_MILLIS =
            "SELECT end_millis FROM cloud_usage.usage_job WHERE end_millis > 0 and success = 1 ORDER BY end_millis DESC LIMIT 1";

    @Override
    public Long checkHeartbeat(final String hostname, final int pid, final int aggregationDuration) {
        final UsageJobVO job = getNextRecurringJob();
        if (job == null) {
            return null;
        }

        if (job.getHost().equals(hostname) && (job.getPid() != null) && (job.getPid().intValue() == pid)) {
            return job.getId();
        }

        final Date lastHeartbeat = job.getHeartbeat();
        if (lastHeartbeat == null) {
            return null;
        }

        final long sinceLastHeartbeat = System.currentTimeMillis() - lastHeartbeat.getTime();

        // TODO:  Make this check a little smarter..but in the mean time we want the mgmt
        //        server to monitor the usage server, we need to make sure other usage
        //        servers take over as the usage job owner more aggressively.  For now
        //        this is hardcoded to 5 minutes.
        if (sinceLastHeartbeat > (5 * 60 * 1000)) {
            return job.getId();
        }
        return null;
    }

    @Override
    public void createNewJob(final String hostname, final int pid, final int jobType) {
        final UsageJobVO newJob = new UsageJobVO();
        newJob.setHost(hostname);
        newJob.setPid(pid);
        newJob.setHeartbeat(new Date());
        newJob.setJobType(jobType);
        persist(newJob);
    }

    @Override
    public UsageJobVO getLastJob() {
        final Filter filter = new Filter(UsageJobVO.class, "id", false, Long.valueOf(0), Long.valueOf(1));
        final SearchCriteria<UsageJobVO> sc = createSearchCriteria();
        sc.addAnd("endMillis", SearchCriteria.Op.EQ, Long.valueOf(0));
        final List<UsageJobVO> jobs = search(sc, filter);

        if ((jobs == null) || jobs.isEmpty()) {
            return null;
        }
        return jobs.get(0);
    }

    @Override
    public UsageJobVO getNextImmediateJob() {
        final Filter filter = new Filter(UsageJobVO.class, "id", false, Long.valueOf(0), Long.valueOf(1));
        final SearchCriteria<UsageJobVO> sc = createSearchCriteria();
        sc.addAnd("endMillis", SearchCriteria.Op.EQ, Long.valueOf(0));
        sc.addAnd("jobType", SearchCriteria.Op.EQ, Integer.valueOf(UsageJobVO.JOB_TYPE_SINGLE));
        sc.addAnd("scheduled", SearchCriteria.Op.EQ, Integer.valueOf(0));
        final List<UsageJobVO> jobs = search(sc, filter);

        if ((jobs == null) || jobs.isEmpty()) {
            return null;
        }
        return jobs.get(0);
    }

    @Override
    public long getLastJobSuccessDateMillis() {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final String sql = GET_LAST_JOB_SUCCESS_DATE_MILLIS;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (final Exception ex) {
            s_logger.error("error getting last usage job success date", ex);
        } finally {
            txn.close();
        }
        return 0L;
    }

    @Override
    public Date getLastHeartbeat() {
        final Filter filter = new Filter(UsageJobVO.class, "heartbeat", false, Long.valueOf(0), Long.valueOf(1));
        final SearchCriteria<UsageJobVO> sc = createSearchCriteria();
        final List<UsageJobVO> jobs = search(sc, filter);

        if ((jobs == null) || jobs.isEmpty()) {
            return null;
        }
        return jobs.get(0).getHeartbeat();
    }

    @Override
    public UsageJobVO isOwner(final String hostname, final int pid) {
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        try {
            if ((hostname == null) || (pid <= 0)) {
                return null;
            }

            final UsageJobVO job = getLastJob();
            if (job == null) {
                return null;
            }

            if (hostname.equals(job.getHost()) && (job.getPid() != null) && (pid == job.getPid().intValue())) {
                return job;
            }
        } finally {
            txn.close();
        }
        return null;
    }

    @Override
    public void updateJobSuccess(final Long jobId, final long startMillis, final long endMillis, final long execTime, final boolean success) {
        final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.USAGE_DB);
        try {
            txn.start();

            final UsageJobVO job = lockRow(jobId, Boolean.TRUE);
            final UsageJobVO jobForUpdate = createForUpdate();
            jobForUpdate.setStartMillis(startMillis);
            jobForUpdate.setEndMillis(endMillis);
            jobForUpdate.setExecTime(execTime);
            jobForUpdate.setStartDate(new Date(startMillis));
            jobForUpdate.setEndDate(new Date(endMillis));
            jobForUpdate.setSuccess(success);
            update(job.getId(), jobForUpdate);

            txn.commit();
        } catch (final Exception ex) {
            txn.rollback();
            s_logger.error("error updating job success date", ex);
            throw new CloudRuntimeException(ex.getMessage());
        } finally {
            txn.close();
        }
    }

    private UsageJobVO getNextRecurringJob() {
        final Filter filter = new Filter(UsageJobVO.class, "id", false, Long.valueOf(0), Long.valueOf(1));
        final SearchCriteria<UsageJobVO> sc = createSearchCriteria();
        sc.addAnd("endMillis", SearchCriteria.Op.EQ, Long.valueOf(0));
        sc.addAnd("jobType", SearchCriteria.Op.EQ, Integer.valueOf(UsageJobVO.JOB_TYPE_RECURRING));
        final List<UsageJobVO> jobs = search(sc, filter);

        if ((jobs == null) || jobs.isEmpty()) {
            return null;
        }
        return jobs.get(0);
    }
}
