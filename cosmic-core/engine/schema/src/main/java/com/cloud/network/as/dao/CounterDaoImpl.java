package com.cloud.network.as.dao;

import com.cloud.network.as.CounterVO;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class CounterDaoImpl extends GenericDaoBase<CounterVO, Long> implements CounterDao {
    final SearchBuilder<CounterVO> AllFieldsSearch;

    protected CounterDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("name", AllFieldsSearch.entity().getName(), Op.LIKE);
        AllFieldsSearch.and("source", AllFieldsSearch.entity().getSource(), Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public List<CounterVO> listCounters(final Long id, final String name, final String source, final String keyword, final Filter filter) {
        final SearchCriteria<CounterVO> sc = AllFieldsSearch.create();

        if (keyword != null) {
            final SearchCriteria<CounterVO> ssc = createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (name != null) {
            sc.addAnd("name", SearchCriteria.Op.LIKE, "%" + name + "%");
        }

        if (id != null) {
            sc.addAnd("id", SearchCriteria.Op.EQ, id);
        }

        if (source != null) {
            sc.addAnd("source", SearchCriteria.Op.EQ, source);
        }
        return listBy(sc, filter);
    }
}
