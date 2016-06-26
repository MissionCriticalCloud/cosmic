package com.cloud.network.as.dao;

import com.cloud.network.as.AutoScalePolicyVO;
import com.cloud.utils.db.GenericDao;

public interface AutoScalePolicyDao extends GenericDao<AutoScalePolicyVO, Long> {
    int removeByAccountId(long accountId);
}
