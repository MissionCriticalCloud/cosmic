package com.cloud.dc.dao;

import com.cloud.dc.StorageNetworkIpRangeVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB
public class StorageNetworkIpRangeDaoImpl extends GenericDaoBase<StorageNetworkIpRangeVO, Long> implements StorageNetworkIpRangeDao {
    protected final GenericSearchBuilder<StorageNetworkIpRangeVO, Long> countRanges;

    protected StorageNetworkIpRangeDaoImpl() {
        countRanges = createSearchBuilder(Long.class);
        countRanges.select(null, Func.COUNT, null);
        countRanges.done();
    }

    @Override
    public List<StorageNetworkIpRangeVO> listByRangeId(final long rangeId) {
        final QueryBuilder<StorageNetworkIpRangeVO> sc = QueryBuilder.create(StorageNetworkIpRangeVO.class);
        sc.and(sc.entity().getId(), Op.EQ, rangeId);
        return sc.list();
    }

    @Override
    public List<StorageNetworkIpRangeVO> listByPodId(final long podId) {
        final QueryBuilder<StorageNetworkIpRangeVO> sc = QueryBuilder.create(StorageNetworkIpRangeVO.class);
        sc.and(sc.entity().getPodId(), Op.EQ, podId);
        return sc.list();
    }

    @Override
    public List<StorageNetworkIpRangeVO> listByDataCenterId(final long dcId) {
        final QueryBuilder<StorageNetworkIpRangeVO> sc = QueryBuilder.create(StorageNetworkIpRangeVO.class);
        sc.and(sc.entity().getDataCenterId(), Op.EQ, dcId);
        return sc.list();
    }

    @Override
    public long countRanges() {
        final SearchCriteria<Long> sc = countRanges.create();
        return customSearch(sc, null).get(0);
    }
}
