package com.cloud.network.dao;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class NetworkDomainDaoImpl extends GenericDaoBase<NetworkDomainVO, Long> implements NetworkDomainDao {
    final SearchBuilder<NetworkDomainVO> AllFieldsSearch;
    final SearchBuilder<NetworkDomainVO> DomainsSearch;

    protected NetworkDomainDaoImpl() {
        super();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("domainId", AllFieldsSearch.entity().getDomainId(), Op.EQ);
        AllFieldsSearch.and("networkId", AllFieldsSearch.entity().getNetworkId(), Op.EQ);
        AllFieldsSearch.done();

        DomainsSearch = createSearchBuilder();
        DomainsSearch.and("domainId", DomainsSearch.entity().getDomainId(), Op.IN);
        DomainsSearch.done();
    }

    @Override
    public List<NetworkDomainVO> listDomainNetworkMapByDomain(final Object... domainId) {
        final SearchCriteria<NetworkDomainVO> sc = DomainsSearch.create();
        sc.setParameters("domainId", domainId);

        return listBy(sc);
    }

    @Override
    public NetworkDomainVO getDomainNetworkMapByNetworkId(final long networkId) {
        final SearchCriteria<NetworkDomainVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);
        return findOneBy(sc);
    }

    @Override
    public List<Long> listNetworkIdsByDomain(final long domainId) {
        final List<Long> networkIdsToReturn = new ArrayList<>();
        final List<NetworkDomainVO> maps = listDomainNetworkMapByDomain(domainId);
        for (final NetworkDomainVO map : maps) {
            networkIdsToReturn.add(map.getNetworkId());
        }
        return networkIdsToReturn;
    }
}
