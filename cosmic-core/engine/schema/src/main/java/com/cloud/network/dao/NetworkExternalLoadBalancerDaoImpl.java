package com.cloud.network.dao;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class NetworkExternalLoadBalancerDaoImpl extends GenericDaoBase<NetworkExternalLoadBalancerVO, Long> implements NetworkExternalLoadBalancerDao {

    final SearchBuilder<NetworkExternalLoadBalancerVO> networkIdSearch;
    final SearchBuilder<NetworkExternalLoadBalancerVO> deviceIdSearch;

    protected NetworkExternalLoadBalancerDaoImpl() {
        super();
        networkIdSearch = createSearchBuilder();
        networkIdSearch.and("networkId", networkIdSearch.entity().getNetworkId(), Op.EQ);
        networkIdSearch.done();

        deviceIdSearch = createSearchBuilder();
        deviceIdSearch.and("externalLBDeviceId", deviceIdSearch.entity().getExternalLBDeviceId(), Op.EQ);
        deviceIdSearch.done();
    }

    @Override
    public NetworkExternalLoadBalancerVO findByNetworkId(final long networkId) {
        final SearchCriteria<NetworkExternalLoadBalancerVO> sc = networkIdSearch.create();
        sc.setParameters("networkId", networkId);
        return findOneBy(sc);
    }

    @Override
    public List<NetworkExternalLoadBalancerVO> listByLoadBalancerDeviceId(final long lbDeviceId) {
        final SearchCriteria<NetworkExternalLoadBalancerVO> sc = deviceIdSearch.create();
        sc.setParameters("externalLBDeviceId", lbDeviceId);
        return search(sc, null);
    }
}
