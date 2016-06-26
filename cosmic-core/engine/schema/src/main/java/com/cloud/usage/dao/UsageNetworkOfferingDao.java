package com.cloud.usage.dao;

import com.cloud.usage.UsageNetworkOfferingVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface UsageNetworkOfferingDao extends GenericDao<UsageNetworkOfferingVO, Long> {
    public void update(UsageNetworkOfferingVO usage);

    public List<UsageNetworkOfferingVO> getUsageRecords(Long accountId, Long domainId, Date startDate, Date endDate, boolean limit, int page);
}
