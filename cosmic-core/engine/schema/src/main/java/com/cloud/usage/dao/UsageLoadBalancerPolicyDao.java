package com.cloud.usage.dao;

import com.cloud.usage.UsageLoadBalancerPolicyVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface UsageLoadBalancerPolicyDao extends GenericDao<UsageLoadBalancerPolicyVO, Long> {
    public void removeBy(long userId, long id);

    public void update(UsageLoadBalancerPolicyVO usage);

    public List<UsageLoadBalancerPolicyVO> getUsageRecords(Long accountId, Long domainId, Date startDate, Date endDate, boolean limit, int page);
}
