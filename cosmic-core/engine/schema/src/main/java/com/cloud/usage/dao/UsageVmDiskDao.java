package com.cloud.usage.dao;

import com.cloud.usage.UsageVmDiskVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;
import java.util.Map;

public interface UsageVmDiskDao extends GenericDao<UsageVmDiskVO, Long> {
    Map<String, UsageVmDiskVO> getRecentVmDiskStats();

    void deleteOldStats(long maxEventTime);

    void saveUsageVmDisks(List<UsageVmDiskVO> usageVmDisks);
}
