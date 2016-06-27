package com.cloud.usage.dao;

import com.cloud.usage.UsageSecurityGroupVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface UsageSecurityGroupDao extends GenericDao<UsageSecurityGroupVO, Long> {
    public void update(UsageSecurityGroupVO usage);

    public List<UsageSecurityGroupVO> getUsageRecords(Long accountId, Long domainId, Date startDate, Date endDate, boolean limit, int page);
}
