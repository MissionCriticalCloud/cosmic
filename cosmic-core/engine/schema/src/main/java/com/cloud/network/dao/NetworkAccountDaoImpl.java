package com.cloud.network.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import org.springframework.stereotype.Component;

@Component
public class NetworkAccountDaoImpl extends GenericDaoBase<NetworkAccountVO, Long> implements NetworkAccountDao {
    final SearchBuilder<NetworkAccountVO> AllFieldsSearch;

    protected NetworkAccountDaoImpl() {
        super();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("networkId", AllFieldsSearch.entity().getNetworkId(), Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public NetworkAccountVO getAccountNetworkMapByNetworkId(final long networkId) {
        final SearchCriteria<NetworkAccountVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);
        return findOneBy(sc);
    }
}
