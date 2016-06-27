package com.cloud.usage.dao;

import com.cloud.usage.UsageVPNUserVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface UsageVPNUserDao extends GenericDao<UsageVPNUserVO, Long> {
    public void update(UsageVPNUserVO usage);

    public List<UsageVPNUserVO> getUsageRecords(Long accountId, Long domainId, Date startDate, Date endDate, boolean limit, int page);
}
