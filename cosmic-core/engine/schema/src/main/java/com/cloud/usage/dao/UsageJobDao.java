package com.cloud.usage.dao;

import com.cloud.usage.UsageJobVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;

public interface UsageJobDao extends GenericDao<UsageJobVO, Long> {
    Long checkHeartbeat(String hostname, int pid, int aggregationDuration);

    void createNewJob(String hostname, int pid, int jobType);

    UsageJobVO getLastJob();

    UsageJobVO getNextImmediateJob();

    long getLastJobSuccessDateMillis();

    Date getLastHeartbeat();

    UsageJobVO isOwner(String hostname, int pid);

    void updateJobSuccess(Long jobId, long startMillis, long endMillis, long execTime, boolean success);
}
