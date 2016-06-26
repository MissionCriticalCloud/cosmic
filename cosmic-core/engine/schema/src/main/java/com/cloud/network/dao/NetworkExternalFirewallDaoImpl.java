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
public class NetworkExternalFirewallDaoImpl extends GenericDaoBase<NetworkExternalFirewallVO, Long> implements NetworkExternalFirewallDao {

    final SearchBuilder<NetworkExternalFirewallVO> networkIdSearch;
    final SearchBuilder<NetworkExternalFirewallVO> deviceIdSearch;

    protected NetworkExternalFirewallDaoImpl() {
        super();
        networkIdSearch = createSearchBuilder();
        networkIdSearch.and("networkId", networkIdSearch.entity().getNetworkId(), Op.EQ);
        networkIdSearch.done();
        deviceIdSearch = createSearchBuilder();
        deviceIdSearch.and("externalFWDeviceId", deviceIdSearch.entity().getExternalFirewallDeviceId(), Op.EQ);
        deviceIdSearch.done();
    }

    @Override
    public NetworkExternalFirewallVO findByNetworkId(final long networkId) {
        final SearchCriteria<NetworkExternalFirewallVO> sc = networkIdSearch.create();
        sc.setParameters("networkId", networkId);
        return findOneBy(sc);
    }

    @Override
    public List<NetworkExternalFirewallVO> listByFirewallDeviceId(final long fwDeviceId) {
        final SearchCriteria<NetworkExternalFirewallVO> sc = deviceIdSearch.create();
        sc.setParameters("externalFWDeviceId", fwDeviceId);
        return search(sc, null);
    }
}
