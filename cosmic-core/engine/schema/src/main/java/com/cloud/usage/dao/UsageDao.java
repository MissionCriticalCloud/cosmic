package com.cloud.usage.dao;

import com.cloud.usage.UsageVO;
import com.cloud.user.AccountVO;
import com.cloud.user.UserStatisticsVO;
import com.cloud.user.VmDiskStatisticsVO;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

public interface UsageDao extends GenericDao<UsageVO, Long> {
    void deleteRecordsForAccount(Long accountId);

    Pair<List<UsageVO>, Integer> searchAndCountAllRecords(SearchCriteria<UsageVO> sc, Filter filter);

    void saveAccounts(List<AccountVO> accounts);

    void updateAccounts(List<AccountVO> accounts);

    void saveUserStats(List<UserStatisticsVO> userStats);

    void updateUserStats(List<UserStatisticsVO> userStats);

    Long getLastAccountId();

    Long getLastUserStatsId();

    List<Long> listPublicTemplatesByAccount(long accountId);

    Long getLastVmDiskStatsId();

    void updateVmDiskStats(List<VmDiskStatisticsVO> vmDiskStats);

    void saveVmDiskStats(List<VmDiskStatisticsVO> vmDiskStats);

    void saveUsageRecords(List<UsageVO> usageRecords);

    void removeOldUsageRecords(int days);

    UsageVO persistUsage(final UsageVO usage);

    Pair<List<? extends UsageVO>, Integer> getUsageRecordsPendingQuotaAggregation(long accountId, long domainId);
}
