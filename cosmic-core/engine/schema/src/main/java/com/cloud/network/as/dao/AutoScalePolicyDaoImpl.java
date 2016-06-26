package com.cloud.network.as.dao;

import com.cloud.network.as.AutoScalePolicyVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchCriteria;

import org.springframework.stereotype.Component;

@Component
public class AutoScalePolicyDaoImpl extends GenericDaoBase<AutoScalePolicyVO, Long> implements AutoScalePolicyDao {

    @Override
    public int removeByAccountId(final long accountId) {
        final SearchCriteria<AutoScalePolicyVO> sc = createSearchCriteria();

        sc.addAnd("accountId", SearchCriteria.Op.EQ, accountId);

        return remove(sc);
    }
}
