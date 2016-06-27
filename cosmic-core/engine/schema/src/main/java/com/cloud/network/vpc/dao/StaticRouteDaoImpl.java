package com.cloud.network.vpc.dao;

import com.cloud.network.vpc.StaticRoute;
import com.cloud.network.vpc.StaticRouteVO;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;

import javax.inject.Inject;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class StaticRouteDaoImpl extends GenericDaoBase<StaticRouteVO, Long> implements StaticRouteDao {
    protected final SearchBuilder<StaticRouteVO> AllFieldsSearch;
    protected final SearchBuilder<StaticRouteVO> NotRevokedSearch;
    protected final GenericSearchBuilder<StaticRouteVO, Long> RoutesByGatewayCount;
    @Inject
    ResourceTagDao _tagsDao;

    protected StaticRouteDaoImpl() {
        super();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("gwIpAddress", AllFieldsSearch.entity().getGwIpAddress(), Op.EQ);
        AllFieldsSearch.and("vpcId", AllFieldsSearch.entity().getVpcId(), Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.done();

        NotRevokedSearch = createSearchBuilder();
        NotRevokedSearch.and("vpcId", NotRevokedSearch.entity().getVpcId(), Op.EQ);
        NotRevokedSearch.and("state", NotRevokedSearch.entity().getState(), Op.NEQ);
        NotRevokedSearch.done();

        RoutesByGatewayCount = createSearchBuilder(Long.class);
        RoutesByGatewayCount.select(null, Func.COUNT, RoutesByGatewayCount.entity().getId());
        RoutesByGatewayCount.and("gwIpAddress", RoutesByGatewayCount.entity().getGwIpAddress(), Op.EQ);
        RoutesByGatewayCount.done();
    }

    @Override
    public boolean setStateToAdd(final StaticRouteVO rule) {
        final SearchCriteria<StaticRouteVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", rule.getId());
        sc.setParameters("state", StaticRoute.State.Staged);

        rule.setState(StaticRoute.State.Add);

        return update(rule, sc) > 0;
    }

    @Override
    public List<? extends StaticRoute> listByVpcIdAndNotRevoked(final long vpcId) {
        final SearchCriteria<StaticRouteVO> sc = NotRevokedSearch.create();
        sc.setParameters("vpcId", vpcId);
        sc.setParameters("state", StaticRoute.State.Revoke);
        return listBy(sc);
    }

    @Override
    public List<StaticRouteVO> listByVpcId(final long vpcId) {
        final SearchCriteria<StaticRouteVO> sc = AllFieldsSearch.create();
        sc.setParameters("vpcId", vpcId);
        return listBy(sc);
    }

    @Override
    @DB
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final StaticRouteVO entry = findById(id);
        if (entry != null) {
            _tagsDao.removeByIdAndType(id, ResourceObjectType.StaticRoute);
        }
        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }
}
