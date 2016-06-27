package com.cloud.network.as.dao;

import com.cloud.network.as.ConditionVO;
import com.cloud.utils.db.GenericDao;

public interface ConditionDao extends GenericDao<ConditionVO, Long> {

    ConditionVO findByCounterId(long ctrId);

    int removeByAccountId(long accountId);
}
