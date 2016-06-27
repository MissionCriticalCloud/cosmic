package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface LBStickinessPolicyDao extends GenericDao<LBStickinessPolicyVO, Long> {
    void remove(long loadBalancerId);

    void remove(long loadBalancerId, Boolean pending);

    List<LBStickinessPolicyVO> listByLoadBalancerIdAndDisplayFlag(long loadBalancerId, boolean forDisplay);

    List<LBStickinessPolicyVO> listByLoadBalancerId(long loadBalancerId, boolean revoke);
}
