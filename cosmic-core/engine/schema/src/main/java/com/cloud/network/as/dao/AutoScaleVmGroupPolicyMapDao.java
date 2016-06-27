package com.cloud.network.as.dao;

import com.cloud.network.as.AutoScaleVmGroupPolicyMapVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface AutoScaleVmGroupPolicyMapDao extends GenericDao<AutoScaleVmGroupPolicyMapVO, Long> {
    boolean removeByGroupId(long vmGroupId);

    boolean removeByGroupAndPolicies(long vmGroupId, List<Long> bakupPolicyIds);

    List<AutoScaleVmGroupPolicyMapVO> listByVmGroupId(long vmGroupId);

    List<AutoScaleVmGroupPolicyMapVO> listByPolicyId(long policyId);

    public boolean isAutoScalePolicyInUse(long policyId);
}
