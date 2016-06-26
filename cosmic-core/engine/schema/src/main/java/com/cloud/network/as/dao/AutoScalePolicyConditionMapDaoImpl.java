package com.cloud.network.as.dao;

import com.cloud.network.as.AutoScalePolicyConditionMapVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AutoScalePolicyConditionMapDaoImpl extends GenericDaoBase<AutoScalePolicyConditionMapVO, Long> implements AutoScalePolicyConditionMapDao {

    @Override
    public List<AutoScalePolicyConditionMapVO> listByAll(final Long policyId, final Long conditionId) {
        return listBy(getSearchCriteria(policyId, conditionId));
    }

    private SearchCriteria<AutoScalePolicyConditionMapVO> getSearchCriteria(final Long policyId, final Long conditionId) {
        final SearchCriteria<AutoScalePolicyConditionMapVO> sc = createSearchCriteria();

        if (policyId != null) {
            sc.addAnd("policyId", SearchCriteria.Op.EQ, policyId);
        }

        if (conditionId != null) {
            sc.addAnd("conditionId", SearchCriteria.Op.EQ, conditionId);
        }

        return sc;
    }

    @Override
    public boolean isConditionInUse(final Long conditionId) {
        return findOneBy(getSearchCriteria(null, conditionId)) != null;
    }

    @Override
    public boolean removeByAutoScalePolicyId(final long policyId) {
        final SearchCriteria<AutoScalePolicyConditionMapVO> sc = createSearchCriteria();
        sc.addAnd("policyId", SearchCriteria.Op.EQ, policyId);
        return expunge(sc) > 0;
    }

    @Override
    public List<AutoScalePolicyConditionMapVO> findByPolicyId(final long policyId) {
        final SearchCriteria<AutoScalePolicyConditionMapVO> sc = createSearchCriteria();
        sc.addAnd("policyId", SearchCriteria.Op.EQ, policyId);

        return listBy(sc);
    }
}
