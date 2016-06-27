package com.cloud.network.as.dao;

import com.cloud.network.as.AutoScaleVmProfileVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchCriteria;

import org.springframework.stereotype.Component;

@Component
public class AutoScaleVmProfileDaoImpl extends GenericDaoBase<AutoScaleVmProfileVO, Long> implements AutoScaleVmProfileDao {

    @Override
    public int removeByAccountId(final long accountId) {
        final SearchCriteria<AutoScaleVmProfileVO> sc = createSearchCriteria();
        sc.addAnd("accountId", SearchCriteria.Op.EQ, accountId);

        return remove(sc);
    }
}
