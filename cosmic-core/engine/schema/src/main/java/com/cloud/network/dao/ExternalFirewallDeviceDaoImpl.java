package com.cloud.network.dao;

import com.cloud.network.dao.ExternalFirewallDeviceVO.FirewallDeviceAllocationState;
import com.cloud.network.dao.ExternalFirewallDeviceVO.FirewallDeviceState;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB
public class ExternalFirewallDeviceDaoImpl extends GenericDaoBase<ExternalFirewallDeviceVO, Long> implements ExternalFirewallDeviceDao {
    final SearchBuilder<ExternalFirewallDeviceVO> physicalNetworkServiceProviderSearch;
    final SearchBuilder<ExternalFirewallDeviceVO> physicalNetworkIdSearch;
    final SearchBuilder<ExternalFirewallDeviceVO> allocationStateSearch;
    final SearchBuilder<ExternalFirewallDeviceVO> deviceStatusSearch;

    protected ExternalFirewallDeviceDaoImpl() {
        physicalNetworkIdSearch = createSearchBuilder();
        physicalNetworkIdSearch.and("physicalNetworkId", physicalNetworkIdSearch.entity().getPhysicalNetworkId(), Op.EQ);
        physicalNetworkIdSearch.done();

        physicalNetworkServiceProviderSearch = createSearchBuilder();
        physicalNetworkServiceProviderSearch.and("physicalNetworkId", physicalNetworkServiceProviderSearch.entity().getPhysicalNetworkId(), Op.EQ);
        physicalNetworkServiceProviderSearch.and("networkServiceProviderName", physicalNetworkServiceProviderSearch.entity().getProviderName(), Op.EQ);
        physicalNetworkServiceProviderSearch.done();

        allocationStateSearch = createSearchBuilder();
        allocationStateSearch.and("physicalNetworkId", allocationStateSearch.entity().getPhysicalNetworkId(), Op.EQ);
        allocationStateSearch.and("providerName", allocationStateSearch.entity().getProviderName(), Op.EQ);
        allocationStateSearch.and("allocationState", allocationStateSearch.entity().getAllocationState(), Op.EQ);
        allocationStateSearch.done();

        deviceStatusSearch = createSearchBuilder();
        deviceStatusSearch.and("physicalNetworkId", deviceStatusSearch.entity().getPhysicalNetworkId(), Op.EQ);
        deviceStatusSearch.and("providerName", deviceStatusSearch.entity().getProviderName(), Op.EQ);
        deviceStatusSearch.and("deviceState", deviceStatusSearch.entity().getDeviceState(), Op.EQ);
        deviceStatusSearch.done();
    }

    @Override
    public List<ExternalFirewallDeviceVO> listByPhysicalNetwork(final long physicalNetworkId) {
        final SearchCriteria<ExternalFirewallDeviceVO> sc = physicalNetworkIdSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);
        return search(sc, null);
    }

    @Override
    public List<ExternalFirewallDeviceVO> listByPhysicalNetworkAndProvider(final long physicalNetworkId, final String providerName) {
        final SearchCriteria<ExternalFirewallDeviceVO> sc = physicalNetworkServiceProviderSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);
        sc.setParameters("networkServiceProviderName", providerName);
        return search(sc, null);
    }

    @Override
    public List<ExternalFirewallDeviceVO> listByProviderAndDeviceAllocationState(final long physicalNetworkId, final String providerName,
                                                                                 final FirewallDeviceAllocationState allocationState) {
        final SearchCriteria<ExternalFirewallDeviceVO> sc = allocationStateSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);
        sc.setParameters("providerName", providerName);
        sc.setParameters("allocationState", allocationState);
        return search(sc, null);
    }

    @Override
    public List<ExternalFirewallDeviceVO> listByProviderAndDeviceStaus(final long physicalNetworkId, final String providerName, final FirewallDeviceState state) {
        final SearchCriteria<ExternalFirewallDeviceVO> sc = deviceStatusSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);
        sc.setParameters("providerName", providerName);
        sc.setParameters("deviceState", state);
        return search(sc, null);
    }
}
