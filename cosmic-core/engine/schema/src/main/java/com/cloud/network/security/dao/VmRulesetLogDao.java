package com.cloud.network.security.dao;

import com.cloud.network.security.VmRulesetLogVO;
import com.cloud.utils.db.GenericDao;

import java.util.Set;

public interface VmRulesetLogDao extends GenericDao<VmRulesetLogVO, Long> {
    VmRulesetLogVO findByVmId(long vmId);

    int createOrUpdate(Set<Long> workItems);
}
