package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface LoadBalancerVMMapDao extends GenericDao<LoadBalancerVMMapVO, Long> {
    void remove(long loadBalancerId);

    void remove(long loadBalancerId, List<Long> instanceIds, Boolean pending);

    List<LoadBalancerVMMapVO> listByInstanceId(long instanceId);

    List<LoadBalancerVMMapVO> listByLoadBalancerId(long loadBalancerId);

    List<LoadBalancerVMMapVO> listByLoadBalancerId(long loadBalancerId, boolean revoke);

    LoadBalancerVMMapVO findByLoadBalancerIdAndVmId(long loadBalancerId, long instanceId);

    boolean isVmAttachedToLoadBalancer(long loadBalancerId);

    List<LoadBalancerVMMapVO> listByInstanceIp(String instanceIp);

    List<LoadBalancerVMMapVO> listByLoadBalancerIdAndVmId(long loadBalancerId, long instanceId);

    LoadBalancerVMMapVO findByLoadBalancerIdAndVmIdVmIp(long loadBalancerId, long instanceId, String instanceIp);

    void remove(long id, long instanceId, String instanceIp, Boolean revoke);
}
