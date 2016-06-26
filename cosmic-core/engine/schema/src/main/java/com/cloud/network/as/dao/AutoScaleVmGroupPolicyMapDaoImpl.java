package com.cloud.network.as.dao;

import com.cloud.network.as.AutoScaleVmGroupPolicyMapVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AutoScaleVmGroupPolicyMapDaoImpl extends GenericDaoBase<AutoScaleVmGroupPolicyMapVO, Long> implements AutoScaleVmGroupPolicyMapDao {

    @Override
    public boolean removeByGroupId(final long vmGroupId) {
        final SearchCriteria<AutoScaleVmGroupPolicyMapVO> sc = createSearchCriteria();
        sc.addAnd("vmGroupId", SearchCriteria.Op.EQ, vmGroupId);

        return expunge(sc) > 0;
    }

    @Override
    public boolean removeByGroupAndPolicies(final long vmGroupId, final List<Long> policyIds) {
        final SearchBuilder<AutoScaleVmGroupPolicyMapVO> policySearch = createSearchBuilder();
        policySearch.and("vmGroupId", policySearch.entity().getVmGroupId(), Op.EQ);
        policySearch.and("policyIds", policySearch.entity().getPolicyId(), Op.IN);
        policySearch.done();
        final SearchCriteria<AutoScaleVmGroupPolicyMapVO> sc = policySearch.create();
        sc.setParameters("vmGroupId", vmGroupId);
        sc.setParameters("policyIds", policyIds);
        return expunge(sc) > 0;
    }

    @Override
    public List<AutoScaleVmGroupPolicyMapVO> listByVmGroupId(final long vmGroupId) {
        final SearchCriteria<AutoScaleVmGroupPolicyMapVO> sc = createSearchCriteria();
        sc.addAnd("vmGroupId", SearchCriteria.Op.EQ, vmGroupId);
        return listBy(sc);
    }

    @Override
    public List<AutoScaleVmGroupPolicyMapVO> listByPolicyId(final long policyId) {
        final SearchCriteria<AutoScaleVmGroupPolicyMapVO> sc = createSearchCriteria();
        sc.addAnd("policyId", SearchCriteria.Op.EQ, policyId);

        return listBy(sc);
    }

    @Override
    public boolean isAutoScalePolicyInUse(final long policyId) {
        final SearchCriteria<AutoScaleVmGroupPolicyMapVO> sc = createSearchCriteria();
        sc.addAnd("policyId", SearchCriteria.Op.EQ, policyId);
        return findOneBy(sc) != null;
    }
}
