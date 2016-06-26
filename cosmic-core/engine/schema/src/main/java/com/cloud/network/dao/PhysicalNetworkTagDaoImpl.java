package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PhysicalNetworkTagDaoImpl extends GenericDaoBase<PhysicalNetworkTagVO, Long> implements GenericDao<PhysicalNetworkTagVO, Long> {
    private final GenericSearchBuilder<PhysicalNetworkTagVO, String> TagSearch;
    private final SearchBuilder<PhysicalNetworkTagVO> AllFieldsSearch;

    protected PhysicalNetworkTagDaoImpl() {
        super();
        TagSearch = createSearchBuilder(String.class);
        TagSearch.selectFields(TagSearch.entity().getTag());
        TagSearch.and("physicalNetworkId", TagSearch.entity().getPhysicalNetworkId(), Op.EQ);
        TagSearch.done();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("physicalNetworkId", AllFieldsSearch.entity().getPhysicalNetworkId(), Op.EQ);
        AllFieldsSearch.and("tag", AllFieldsSearch.entity().getTag(), Op.EQ);
        AllFieldsSearch.done();
    }

    public List<String> getTags(final long physicalNetworkId) {
        final SearchCriteria<String> sc = TagSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);

        return customSearch(sc, null);
    }

    public int clearTags(final long physicalNetworkId) {
        final SearchCriteria<PhysicalNetworkTagVO> sc = AllFieldsSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);

        return remove(sc);
    }
}
