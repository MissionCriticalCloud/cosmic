package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PhysicalNetworkServiceProviderDao extends GenericDao<PhysicalNetworkServiceProviderVO, Long> {
    List<PhysicalNetworkServiceProviderVO> listBy(long physicalNetworkId);

    PhysicalNetworkServiceProviderVO findByServiceProvider(long physicalNetworkId, String providerType);

    void deleteProviders(long physicalNetworkId);

    boolean isServiceProviderEnabled(long physicalNetworkId, String providerType, String serviceType);
}
