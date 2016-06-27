package com.cloud.network.dao;

import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.utils.db.GenericDao;

import java.util.List;

/**
 * NetworkServiceDao deals with searches and operations done on the
 * ntwk_service_map table.
 */
public interface NetworkServiceMapDao extends GenericDao<NetworkServiceMapVO, Long> {
    boolean areServicesSupportedInNetwork(long networkId, Service... services);

    boolean canProviderSupportServiceInNetwork(long networkId, Service service, Provider provider);

    List<NetworkServiceMapVO> getServicesInNetwork(long networkId);

    String getProviderForServiceInNetwork(long networkid, Service service);

    void deleteByNetworkId(long networkId);

    List<String> getDistinctProviders(long networkId);

    String isProviderForNetwork(long networkId, Provider provider);

    List<String> getProvidersForServiceInNetwork(long networkId, Service service);
}
