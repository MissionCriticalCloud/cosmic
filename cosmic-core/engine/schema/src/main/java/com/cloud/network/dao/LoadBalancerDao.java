package com.cloud.network.dao;

import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface LoadBalancerDao extends GenericDao<LoadBalancerVO, Long> {

    List<LoadBalancerVO> listByIpAddress(long ipAddressId);

    List<LoadBalancerVO> listByNetworkIdAndScheme(long networkId, Scheme scheme);

    List<LoadBalancerVO> listInTransitionStateByNetworkIdAndScheme(long networkId, Scheme scheme);
}
