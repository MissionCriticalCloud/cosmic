package com.cloud.network.dao;

import com.cloud.network.LBHealthCheckPolicyVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface LBHealthCheckPolicyDao extends GenericDao<LBHealthCheckPolicyVO, Long> {
    void remove(long loadBalancerId);

    void remove(long loadBalancerId, Boolean pending);

    List<LBHealthCheckPolicyVO> listByLoadBalancerIdAndDisplayFlag(long loadBalancerId, Boolean forDisplay);

    List<LBHealthCheckPolicyVO> listByLoadBalancerId(long loadBalancerId, boolean revoke);
}
