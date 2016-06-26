package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface NetworkExternalLoadBalancerDao extends GenericDao<NetworkExternalLoadBalancerVO, Long> {

    /**
     * find the network to load balancer device mapping corresponding to a network
     *
     * @param networkId guest network Id
     * @return return NetworkExternalLoadBalancerVO for the guest network
     */
    NetworkExternalLoadBalancerVO findByNetworkId(long networkId);

    /**
     * list all network to load balancer device mappings corresponding to a load balancer device Id
     *
     * @param lbDeviceId load balancer device Id
     * @return list of NetworkExternalLoadBalancerVO mappings corresponding to the networks mapped to the load balancer device
     */
    List<NetworkExternalLoadBalancerVO> listByLoadBalancerDeviceId(long lbDeviceId);
}
