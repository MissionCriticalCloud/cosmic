package com.cloud.vpc.dao;

import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.dao.NetworkServiceMapDao;
import com.cloud.network.dao.NetworkServiceMapVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;

import java.util.List;

@DB()
public class MockNetworkServiceMapDaoImpl extends GenericDaoBase<NetworkServiceMapVO, Long> implements NetworkServiceMapDao {

    /* (non-Javadoc)
     * @see com.cloud.network.dao.NetworkServiceMapDao#areServicesSupportedInNetwork(long, com.cloud.network.Network.Service[])
     */
    @Override
    public boolean areServicesSupportedInNetwork(final long networkId, final Service... services) {
        if (services.length > 0 && services[0] == Service.Lb) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.dao.NetworkServiceMapDao#canProviderSupportServiceInNetwork(long, com.cloud.network.Network.Service, com.cloud.network.Network.Provider)
     */
    @Override
    public boolean canProviderSupportServiceInNetwork(final long networkId, final Service service, final Provider provider) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.dao.NetworkServiceMapDao#getServicesInNetwork(long)
     */
    @Override
    public List<NetworkServiceMapVO> getServicesInNetwork(final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.dao.NetworkServiceMapDao#getProviderForServiceInNetwork(long, com.cloud.network.Network.Service)
     */
    @Override
    public String getProviderForServiceInNetwork(final long networkid, final Service service) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.dao.NetworkServiceMapDao#deleteByNetworkId(long)
     */
    @Override
    public void deleteByNetworkId(final long networkId) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.network.dao.NetworkServiceMapDao#getDistinctProviders(long)
     */
    @Override
    public List<String> getDistinctProviders(final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.dao.NetworkServiceMapDao#isProviderForNetwork(long, com.cloud.network.Network.Provider)
     */
    @Override
    public String isProviderForNetwork(final long networkId, final Provider provider) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getProvidersForServiceInNetwork(final long networkId, final Service service) {
        // TODO Auto-generated method stub
        return null;
    }
}
