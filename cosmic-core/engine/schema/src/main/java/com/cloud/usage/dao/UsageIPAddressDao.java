package com.cloud.usage.dao;

import com.cloud.usage.UsageIPAddressVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface UsageIPAddressDao extends GenericDao<UsageIPAddressVO, Long> {
    public void update(UsageIPAddressVO usage);

    public List<UsageIPAddressVO> getUsageRecords(Long accountId, Long domainId, Date startDate, Date endDate);
}
