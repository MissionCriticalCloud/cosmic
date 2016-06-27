package org.apache.cloudstack.framework.jobs.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.framework.jobs.impl.AsyncJobJoinMapVO;
import org.apache.cloudstack.jobs.JobInfo;

import java.util.Date;
import java.util.List;

public interface AsyncJobJoinMapDao extends GenericDao<AsyncJobJoinMapVO, Long> {

    Long joinJob(long jobId, long joinJobId, long joinMsid, long wakeupIntervalMs, long expirationMs, Long syncSourceId, String wakeupHandler, String wakeupDispatcher);

    void disjoinJob(long jobId, long joinedJobId);

    void disjoinAllJobs(long jobId);

    AsyncJobJoinMapVO getJoinRecord(long jobId, long joinJobId);

    List<AsyncJobJoinMapVO> listJoinRecords(long jobId);

    void completeJoin(long joinJobId, JobInfo.Status joinStatus, String joinResult, long completeMsid);

    //    List<Long> wakeupScan();

    List<Long> findJobsToWake(long joinedJobId);

    List<Long> findJobsToWakeBetween(Date cutDate);
    //    List<Long> wakeupByJoinedJobCompletion(long joinedJobId);
}
