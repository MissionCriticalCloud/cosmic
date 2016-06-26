package com.cloud.network.dao;

import com.cloud.network.Networks.TrafficType;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;

import javax.inject.Inject;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class PhysicalNetworkDaoImpl extends GenericDaoBase<PhysicalNetworkVO, Long> implements PhysicalNetworkDao {
    final SearchBuilder<PhysicalNetworkVO> ZoneSearch;

    @Inject
    protected PhysicalNetworkTrafficTypeDao _trafficTypeDao;

    protected PhysicalNetworkDaoImpl() {
        super();
        ZoneSearch = createSearchBuilder();
        ZoneSearch.and("dataCenterId", ZoneSearch.entity().getDataCenterId(), Op.EQ);
        ZoneSearch.done();
    }

    @Override
    public List<PhysicalNetworkVO> listByZone(final long zoneId) {
        final SearchCriteria<PhysicalNetworkVO> sc = ZoneSearch.create();
        sc.setParameters("dataCenterId", zoneId);
        return search(sc, null);
    }

    @Override
    public List<PhysicalNetworkVO> listByZoneIncludingRemoved(final long zoneId) {
        final SearchCriteria<PhysicalNetworkVO> sc = ZoneSearch.create();
        sc.setParameters("dataCenterId", zoneId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<PhysicalNetworkVO> listByZoneAndTrafficType(final long dataCenterId, final TrafficType trafficType) {

        final SearchBuilder<PhysicalNetworkTrafficTypeVO> trafficTypeSearch = _trafficTypeDao.createSearchBuilder();
        final PhysicalNetworkTrafficTypeVO trafficTypeEntity = trafficTypeSearch.entity();
        trafficTypeSearch.and("trafficType", trafficTypeSearch.entity().getTrafficType(), SearchCriteria.Op.EQ);

        final SearchBuilder<PhysicalNetworkVO> pnSearch = createSearchBuilder();
        pnSearch.and("dataCenterId", pnSearch.entity().getDataCenterId(), Op.EQ);
        pnSearch.join("trafficTypeSearch", trafficTypeSearch, pnSearch.entity().getId(), trafficTypeEntity.getPhysicalNetworkId(), JoinBuilder.JoinType.INNER);

        final SearchCriteria<PhysicalNetworkVO> sc = pnSearch.create();
        sc.setJoinParameters("trafficTypeSearch", "trafficType", trafficType);
        sc.setParameters("dataCenterId", dataCenterId);

        return listBy(sc);
    }
}
