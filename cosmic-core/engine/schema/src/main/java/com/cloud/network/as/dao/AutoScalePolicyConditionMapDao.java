package com.cloud.network.as.dao;

import com.cloud.network.as.AutoScalePolicyConditionMapVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface AutoScalePolicyConditionMapDao extends GenericDao<AutoScalePolicyConditionMapVO, Long> {
    List<AutoScalePolicyConditionMapVO> listByAll(Long policyId, Long conditionId);

    public boolean isConditionInUse(Long conditionId);

    boolean removeByAutoScalePolicyId(long id);

    List<AutoScalePolicyConditionMapVO> findByPolicyId(long id);
}
