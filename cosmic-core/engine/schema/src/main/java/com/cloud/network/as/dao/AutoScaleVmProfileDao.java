package com.cloud.network.as.dao;

import com.cloud.network.as.AutoScaleVmProfileVO;
import com.cloud.utils.db.GenericDao;

public interface AutoScaleVmProfileDao extends GenericDao<AutoScaleVmProfileVO, Long> {

    int removeByAccountId(long accountId);
}
