package com.cloud.network.vpc.dao;

import com.cloud.network.vpc.StaticRoute;
import com.cloud.network.vpc.StaticRouteVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface StaticRouteDao extends GenericDao<StaticRouteVO, Long> {

    boolean setStateToAdd(StaticRouteVO rule);

    List<? extends StaticRoute> listByVpcIdAndNotRevoked(long vpcId);

    List<StaticRouteVO> listByVpcId(long vpcId);
}
