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
public class PhysicalNetworkIsolationMethodDaoImpl extends GenericDaoBase<PhysicalNetworkIsolationMethodVO, Long> implements
        GenericDao<PhysicalNetworkIsolationMethodVO, Long> {
    private final GenericSearchBuilder<PhysicalNetworkIsolationMethodVO, String> IsolationMethodSearch;
    private final SearchBuilder<PhysicalNetworkIsolationMethodVO> AllFieldsSearch;

    protected PhysicalNetworkIsolationMethodDaoImpl() {
        super();
        IsolationMethodSearch = createSearchBuilder(String.class);
        IsolationMethodSearch.selectFields(IsolationMethodSearch.entity().getIsolationMethod());
        IsolationMethodSearch.and("physicalNetworkId", IsolationMethodSearch.entity().getPhysicalNetworkId(), Op.EQ);
        IsolationMethodSearch.done();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("physicalNetworkId", AllFieldsSearch.entity().getPhysicalNetworkId(), Op.EQ);
        AllFieldsSearch.and("isolationMethod", AllFieldsSearch.entity().getIsolationMethod(), Op.EQ);
        AllFieldsSearch.done();
    }

    public List<String> getAllIsolationMethod(final long physicalNetworkId) {
        final SearchCriteria<String> sc = IsolationMethodSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);

        return customSearch(sc, null);
    }

    public String getIsolationMethod(final long physicalNetworkId) {
        final SearchCriteria<String> sc = IsolationMethodSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);

        return customSearch(sc, null).get(0);
    }

    public int clearIsolationMethods(final long physicalNetworkId) {
        final SearchCriteria<PhysicalNetworkIsolationMethodVO> sc = AllFieldsSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);

        return remove(sc);
    }
}
