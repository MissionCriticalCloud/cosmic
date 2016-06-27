package com.cloud.usage.dao;

import com.cloud.usage.UsageStorageVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface UsageStorageDao extends GenericDao<UsageStorageVO, Long> {
    public void removeBy(long userId, long id, int storageType);

    public void update(UsageStorageVO usage);

    public List<UsageStorageVO> getUsageRecords(Long accountId, Long domainId, Date startDate, Date endDate, boolean limit, int page);

    List<UsageStorageVO> listById(long accountId, long id, int type);

    List<UsageStorageVO> listByIdAndZone(long accountId, long id, int type, long dcId);
}
