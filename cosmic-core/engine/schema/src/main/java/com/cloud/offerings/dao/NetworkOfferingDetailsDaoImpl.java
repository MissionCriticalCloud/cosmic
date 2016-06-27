package com.cloud.offerings.dao;

import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Detail;
import com.cloud.offerings.NetworkOfferingDetailsVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkOfferingDetailsDaoImpl extends GenericDaoBase<NetworkOfferingDetailsVO, Long> implements NetworkOfferingDetailsDao {
    protected final SearchBuilder<NetworkOfferingDetailsVO> DetailSearch;
    private final GenericSearchBuilder<NetworkOfferingDetailsVO, String> ValueSearch;

    public NetworkOfferingDetailsDaoImpl() {

        DetailSearch = createSearchBuilder();
        DetailSearch.and("offeringId", DetailSearch.entity().getOfferingId(), SearchCriteria.Op.EQ);
        DetailSearch.and("name", DetailSearch.entity().getName(), SearchCriteria.Op.EQ);
        DetailSearch.done();

        ValueSearch = createSearchBuilder(String.class);
        ValueSearch.select(null, Func.DISTINCT, ValueSearch.entity().getValue());
        ValueSearch.and("offeringId", ValueSearch.entity().getOfferingId(), SearchCriteria.Op.EQ);
        ValueSearch.and("name", ValueSearch.entity().getName(), Op.EQ);
        ValueSearch.done();
    }

    @Override
    public Map<NetworkOffering.Detail, String> getNtwkOffDetails(final long offeringId) {
        final SearchCriteria<NetworkOfferingDetailsVO> sc = DetailSearch.create();
        sc.setParameters("offeringId", offeringId);

        final List<NetworkOfferingDetailsVO> results = search(sc, null);
        final Map<NetworkOffering.Detail, String> details = new HashMap<>(results.size());
        for (final NetworkOfferingDetailsVO result : results) {
            details.put(result.getName(), result.getValue());
        }

        return details;
    }

    @Override
    public String getDetail(final long offeringId, final Detail detailName) {
        final SearchCriteria<String> sc = ValueSearch.create();
        sc.setParameters("name", detailName);
        sc.setParameters("offeringId", offeringId);
        final List<String> results = customSearch(sc, null);
        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }
}
