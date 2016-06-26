package com.cloud.usage.dao;

import com.cloud.usage.UsageNetworkVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;
import java.util.Map;

public interface UsageNetworkDao extends GenericDao<UsageNetworkVO, Long> {
    Map<String, UsageNetworkVO> getRecentNetworkStats();

    void deleteOldStats(long maxEventTime);

    void saveUsageNetworks(List<UsageNetworkVO> usageNetworks);
}
