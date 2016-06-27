package com.cloud.network.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class RouterNetworkDaoImpl extends GenericDaoBase<RouterNetworkVO, Long> implements RouterNetworkDao {
    protected final GenericSearchBuilder<RouterNetworkVO, Long> RouterNetworksSearch;
    protected final SearchBuilder<RouterNetworkVO> AllFieldsSearch;

    public RouterNetworkDaoImpl() {
        super();

        RouterNetworksSearch = createSearchBuilder(Long.class);
        RouterNetworksSearch.selectFields(RouterNetworksSearch.entity().getNetworkId());
        RouterNetworksSearch.and("routerId", RouterNetworksSearch.entity().getRouterId(), Op.EQ);
        RouterNetworksSearch.done();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("routerId", AllFieldsSearch.entity().getRouterId(), Op.EQ);
        AllFieldsSearch.and("networkId", AllFieldsSearch.entity().getNetworkId(), Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public List<Long> getRouterNetworks(final long routerId) {
        final SearchCriteria<Long> sc = RouterNetworksSearch.create();
        sc.setParameters("routerId", routerId);
        return customSearch(sc, null);
    }

    @Override
    public RouterNetworkVO findByRouterAndNetwork(final long routerId, final long networkId) {
        final SearchCriteria<RouterNetworkVO> sc = AllFieldsSearch.create();
        sc.setParameters("routerId", routerId);
        sc.setParameters("networkId", networkId);
        return findOneBy(sc);
    }
}
