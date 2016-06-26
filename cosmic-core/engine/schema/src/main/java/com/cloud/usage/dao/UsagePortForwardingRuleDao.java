package com.cloud.usage.dao;

import com.cloud.usage.UsagePortForwardingRuleVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface UsagePortForwardingRuleDao extends GenericDao<UsagePortForwardingRuleVO, Long> {
    public void removeBy(long userId, long id);

    public void update(UsagePortForwardingRuleVO usage);

    public List<UsagePortForwardingRuleVO> getUsageRecords(Long accountId, Long domainId, Date startDate, Date endDate, boolean limit, int page);
}
