package com.cloud.network.as.dao;

import com.cloud.network.as.ConditionVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import org.springframework.stereotype.Component;

@Component
public class ConditionDaoImpl extends GenericDaoBase<ConditionVO, Long> implements ConditionDao {
    final SearchBuilder<ConditionVO> AllFieldsSearch;

    protected ConditionDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("counterId", AllFieldsSearch.entity().getCounterid(), Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public ConditionVO findByCounterId(final long ctrId) {
        final SearchCriteria<ConditionVO> sc = AllFieldsSearch.create();
        sc.setParameters("counterId", ctrId);
        return findOneBy(sc);
    }

    @Override
    public int removeByAccountId(final long accountId) {
        final SearchCriteria<ConditionVO> sc = createSearchCriteria();

        sc.addAnd("accountId", SearchCriteria.Op.EQ, accountId);

        return remove(sc);
    }
}
