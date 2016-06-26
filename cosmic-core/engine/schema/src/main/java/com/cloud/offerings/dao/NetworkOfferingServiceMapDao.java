package com.cloud.offerings.dao;

import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.offerings.NetworkOfferingServiceMapVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

/**
 * NetworkOfferingServiceDao deals with searches and operations done on the
 * ntwk_offering_service_map table.
 */
public interface NetworkOfferingServiceMapDao extends GenericDao<NetworkOfferingServiceMapVO, Long> {
    boolean areServicesSupportedByNetworkOffering(long networkOfferingId, Service... services);

    List<NetworkOfferingServiceMapVO> listByNetworkOfferingId(long networkOfferingId);

    void deleteByOfferingId(long networkOfferingId);

    List<String> listProvidersForServiceForNetworkOffering(long networkOfferingId, Service service);

    boolean isProviderForNetworkOffering(long networkOfferingId, Provider provider);

    List<String> listServicesForNetworkOffering(long networkOfferingId);

    List<String> getDistinctProviders(long offId);
}
