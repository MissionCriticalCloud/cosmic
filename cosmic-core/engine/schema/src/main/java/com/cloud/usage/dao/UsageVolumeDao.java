package com.cloud.usage.dao;

import com.cloud.usage.UsageVolumeVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface UsageVolumeDao extends GenericDao<UsageVolumeVO, Long> {
    public void removeBy(long userId, long id);

    public void update(UsageVolumeVO usage);

    public List<UsageVolumeVO> getUsageRecords(Long accountId, Long domainId, Date startDate, Date endDate, boolean limit, int page);
}
