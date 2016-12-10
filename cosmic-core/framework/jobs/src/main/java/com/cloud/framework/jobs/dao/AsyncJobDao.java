package com.cloud.framework.jobs.dao;

import com.cloud.framework.jobs.impl.AsyncJobVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface AsyncJobDao extends GenericDao<AsyncJobVO, Long> {
    AsyncJobVO findInstancePendingAsyncJob(String instanceType, long instanceId);

    List<AsyncJobVO> findInstancePendingAsyncJobs(String instanceType, Long accountId);

    AsyncJobVO findPseudoJob(long threadId, long msid);

    void cleanupPseduoJobs(long msid);

    List<AsyncJobVO> getExpiredJobs(Date cutTime, int limit);

    List<AsyncJobVO> getExpiredUnfinishedJobs(Date cutTime, int limit);

    void resetJobProcess(long msid, int jobResultCode, String jobResultMessage);

    List<AsyncJobVO> getExpiredCompletedJobs(Date cutTime, int limit);

    List<AsyncJobVO> getResetJobs(long msid);

    List<AsyncJobVO> getFailureJobsSinceLastMsStart(long msId, String... cmds);
}
