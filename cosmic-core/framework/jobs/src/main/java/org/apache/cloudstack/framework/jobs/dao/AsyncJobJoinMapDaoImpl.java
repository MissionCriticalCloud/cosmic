package org.apache.cloudstack.framework.jobs.dao;

import com.cloud.utils.DateUtil;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.framework.jobs.impl.AsyncJobJoinMapVO;
import org.apache.cloudstack.jobs.JobInfo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncJobJoinMapDaoImpl extends GenericDaoBase<AsyncJobJoinMapVO, Long> implements AsyncJobJoinMapDao {
    public static final Logger s_logger = LoggerFactory.getLogger(AsyncJobJoinMapDaoImpl.class);

    private final SearchBuilder<AsyncJobJoinMapVO> RecordSearch;
    private final SearchBuilder<AsyncJobJoinMapVO> RecordSearchByOwner;
    private final SearchBuilder<AsyncJobJoinMapVO> CompleteJoinSearch;
    private final SearchBuilder<AsyncJobJoinMapVO> WakeupSearch;

    protected AsyncJobJoinMapDaoImpl() {
        RecordSearch = createSearchBuilder();
        RecordSearch.and("jobId", RecordSearch.entity().getJobId(), Op.EQ);
        RecordSearch.and("joinJobId", RecordSearch.entity().getJoinJobId(), Op.EQ);
        RecordSearch.done();

        RecordSearchByOwner = createSearchBuilder();
        RecordSearchByOwner.and("jobId", RecordSearchByOwner.entity().getJobId(), Op.EQ);
        RecordSearchByOwner.done();

        CompleteJoinSearch = createSearchBuilder();
        CompleteJoinSearch.and("joinJobId", CompleteJoinSearch.entity().getJoinJobId(), Op.EQ);
        CompleteJoinSearch.done();

        WakeupSearch = createSearchBuilder();
        WakeupSearch.and("nextWakeupTime", WakeupSearch.entity().getNextWakeupTime(), Op.LT);
        WakeupSearch.and("expiration", WakeupSearch.entity().getExpiration(), Op.GT);
        WakeupSearch.and("joinStatus", WakeupSearch.entity().getJoinStatus(), Op.EQ);
        WakeupSearch.done();
    }

    @Override
    public Long joinJob(final long jobId, final long joinJobId, final long joinMsid, final long wakeupIntervalMs, final long expirationMs, final Long syncSourceId, final String
            wakeupHandler,
                        final String wakeupDispatcher) {

        final AsyncJobJoinMapVO record = new AsyncJobJoinMapVO();
        record.setJobId(jobId);
        record.setJoinJobId(joinJobId);
        record.setJoinMsid(joinMsid);
        record.setJoinStatus(JobInfo.Status.IN_PROGRESS);
        record.setSyncSourceId(syncSourceId);
        record.setWakeupInterval(wakeupIntervalMs / 1000);        // convert millisecond to second
        record.setWakeupHandler(wakeupHandler);
        record.setWakeupDispatcher(wakeupDispatcher);
        if (wakeupHandler != null) {
            record.setNextWakeupTime(new Date(DateUtil.currentGMTTime().getTime() + wakeupIntervalMs));
            record.setExpiration(new Date(DateUtil.currentGMTTime().getTime() + expirationMs));
        }

        persist(record);
        return record.getId();
    }

    @Override
    public void disjoinJob(final long jobId, final long joinedJobId) {
        final SearchCriteria<AsyncJobJoinMapVO> sc = RecordSearch.create();
        sc.setParameters("jobId", jobId);
        sc.setParameters("joinJobId", joinedJobId);

        this.expunge(sc);
    }

    @Override
    public void disjoinAllJobs(final long jobId) {
        final SearchCriteria<AsyncJobJoinMapVO> sc = RecordSearchByOwner.create();
        sc.setParameters("jobId", jobId);

        this.expunge(sc);
    }

    @Override
    public AsyncJobJoinMapVO getJoinRecord(final long jobId, final long joinJobId) {
        final SearchCriteria<AsyncJobJoinMapVO> sc = RecordSearch.create();
        sc.setParameters("jobId", jobId);
        sc.setParameters("joinJobId", joinJobId);

        final List<AsyncJobJoinMapVO> result = this.listBy(sc);
        if (result != null && result.size() > 0) {
            assert (result.size() == 1);
            return result.get(0);
        }

        return null;
    }

    @Override
    public List<AsyncJobJoinMapVO> listJoinRecords(final long jobId) {
        final SearchCriteria<AsyncJobJoinMapVO> sc = RecordSearchByOwner.create();
        sc.setParameters("jobId", jobId);

        return this.listBy(sc);
    }

    @Override
    public void completeJoin(final long joinJobId, final JobInfo.Status joinStatus, final String joinResult, final long completeMsid) {
        final AsyncJobJoinMapVO record = createForUpdate();
        record.setJoinStatus(joinStatus);
        record.setJoinResult(joinResult);
        record.setCompleteMsid(completeMsid);
        record.setLastUpdated(DateUtil.currentGMTTime());

        final UpdateBuilder ub = getUpdateBuilder(record);

        final SearchCriteria<AsyncJobJoinMapVO> sc = CompleteJoinSearch.create();
        sc.setParameters("joinJobId", joinJobId);
        update(ub, sc, null);
    }

    @Override
    public List<Long> findJobsToWake(final long joinedJobId) {
        // TODO: We should fix this.  We shouldn't be crossing daos in a dao code.
        final List<Long> standaloneList = new ArrayList<>();
        final String sql = "SELECT job_id FROM async_job_join_map WHERE join_job_id = ? AND job_id NOT IN (SELECT content_id FROM sync_queue_item)";
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn(); PreparedStatement pstmt = txn.prepareStatement(sql)) {
            pstmt.setLong(1, joinedJobId);
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                standaloneList.add(rs.getLong(1));
            }
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable to execute " + sql, e);
        }
        return standaloneList;
    }

    @Override
    public List<Long> findJobsToWakeBetween(final Date cutDate) {
        final List<Long> standaloneList = new ArrayList<>();

        String sql = "SELECT job_id FROM async_job_join_map WHERE next_wakeup < ? AND expiration > ? AND job_id NOT IN (SELECT content_id FROM sync_queue_item)";
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn(); PreparedStatement pstmt = txn.prepareStatement(sql)) {
            pstmt.setString(1, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), cutDate));
            pstmt.setString(2, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), cutDate));
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                standaloneList.add(rs.getLong(1));
            }
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable to handle SQL exception", e);
        }

        // update for next wake-up
        sql = "UPDATE async_job_join_map SET next_wakeup=DATE_ADD(next_wakeup, INTERVAL wakeup_interval SECOND) WHERE next_wakeup < ? AND expiration > ?";
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn(); PreparedStatement pstmt = txn.prepareStatement(sql)) {
            pstmt.setString(1, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), cutDate));
            pstmt.setString(2, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), cutDate));
            pstmt.executeUpdate();

            return standaloneList;
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable to handle SQL exception", e);
        }
    }
}
