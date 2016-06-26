package com.cloud.network.dao;

import com.cloud.utils.db.Attribute;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.UpdateBuilder;

import org.springframework.stereotype.Component;

@Component
public class NetworkOpDaoImpl extends GenericDaoBase<NetworkOpVO, Long> implements NetworkOpDao {
    protected final SearchBuilder<NetworkOpVO> AllFieldsSearch;
    protected final GenericSearchBuilder<NetworkOpVO, Integer> ActiveNicsSearch;
    protected final Attribute _activeNicsAttribute;

    protected NetworkOpDaoImpl() {
        super();

        ActiveNicsSearch = createSearchBuilder(Integer.class);
        ActiveNicsSearch.selectFields(ActiveNicsSearch.entity().getActiveNicsCount());
        ActiveNicsSearch.and("network", ActiveNicsSearch.entity().getId(), Op.EQ);
        ActiveNicsSearch.done();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("network", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.done();

        _activeNicsAttribute = _allAttributes.get("activeNicsCount");
        assert _activeNicsAttribute != null : "Cannot find activeNicsCount";
    }

    @Override
    public int getActiveNics(final long networkId) {
        final SearchCriteria<Integer> sc = ActiveNicsSearch.create();
        sc.setParameters("network", networkId);

        return customSearch(sc, null).get(0);
    }

    @Override
    public void changeActiveNicsBy(final long networkId, final int count) {

        final SearchCriteria<NetworkOpVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);

        final NetworkOpVO vo = createForUpdate();
        final UpdateBuilder builder = getUpdateBuilder(vo);
        builder.incr(_activeNicsAttribute, count);

        update(builder, sc, null);
    }

    @Override
    public void setCheckForGc(final long networkId) {
        final NetworkOpVO vo = createForUpdate();
        vo.setCheckForGc(true);
        update(networkId, vo);
    }

    @Override
    public void clearCheckForGc(final long networkId) {
        final NetworkOpVO vo = createForUpdate();
        vo.setCheckForGc(false);
        update(networkId, vo);
    }
}
