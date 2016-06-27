package com.cloud.network.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class LoadBalancerVMMapDaoImpl extends GenericDaoBase<LoadBalancerVMMapVO, Long> implements LoadBalancerVMMapDao {

    @Override
    public void remove(final long loadBalancerId) {
        final SearchCriteria<LoadBalancerVMMapVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);

        expunge(sc);
    }

    @Override
    public void remove(final long loadBalancerId, final List<Long> instanceIds, final Boolean revoke) {
        final SearchCriteria<LoadBalancerVMMapVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        sc.addAnd("instanceId", SearchCriteria.Op.IN, instanceIds.toArray());
        if (revoke != null) {
            sc.addAnd("revoke", SearchCriteria.Op.EQ, revoke);
        }

        expunge(sc);
    }

    @Override
    public List<LoadBalancerVMMapVO> listByInstanceId(final long instanceId) {
        final SearchCriteria<LoadBalancerVMMapVO> sc = createSearchCriteria();
        sc.addAnd("instanceId", SearchCriteria.Op.EQ, instanceId);

        return listBy(sc);
    }

    @Override
    public List<LoadBalancerVMMapVO> listByLoadBalancerId(final long loadBalancerId) {
        final SearchCriteria<LoadBalancerVMMapVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);

        return listBy(sc);
    }

    @Override
    public List<LoadBalancerVMMapVO> listByLoadBalancerId(final long loadBalancerId, final boolean pending) {
        final SearchCriteria<LoadBalancerVMMapVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        sc.addAnd("revoke", SearchCriteria.Op.EQ, pending);

        return listBy(sc);
    }

    @Override
    public LoadBalancerVMMapVO findByLoadBalancerIdAndVmId(final long loadBalancerId, final long instanceId) {
        final SearchCriteria<LoadBalancerVMMapVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        sc.addAnd("instanceId", SearchCriteria.Op.EQ, instanceId);
        return findOneBy(sc);
    }

    @Override
    public boolean isVmAttachedToLoadBalancer(final long loadBalancerId) {
        final GenericSearchBuilder<LoadBalancerVMMapVO, Long> CountByAccount = createSearchBuilder(Long.class);
        CountByAccount.select(null, Func.COUNT, null);
        CountByAccount.and("loadBalancerId", CountByAccount.entity().getLoadBalancerId(), SearchCriteria.Op.EQ);

        final SearchCriteria<Long> sc = CountByAccount.create();
        sc.setParameters("loadBalancerId", loadBalancerId);
        return customSearch(sc, null).get(0) > 0;
    }

    @Override
    public List<LoadBalancerVMMapVO> listByInstanceIp(final String instanceIp) {
        final SearchCriteria<LoadBalancerVMMapVO> sc = createSearchCriteria();
        sc.addAnd("instanceIp", SearchCriteria.Op.EQ, instanceIp);

        return listBy(sc);
    }

    @Override
    public List<LoadBalancerVMMapVO> listByLoadBalancerIdAndVmId(final long loadBalancerId, final long instanceId) {
        final SearchCriteria<LoadBalancerVMMapVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        sc.addAnd("instanceId", SearchCriteria.Op.EQ, instanceId);
        return listBy(sc);
    }

    @Override
    public LoadBalancerVMMapVO findByLoadBalancerIdAndVmIdVmIp(final long loadBalancerId, final long instanceId, final String instanceIp) {
        final SearchCriteria<LoadBalancerVMMapVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        sc.addAnd("instanceId", SearchCriteria.Op.EQ, instanceId);
        sc.addAnd("instanceIp", SearchCriteria.Op.EQ, instanceIp);

        return findOneBy(sc);
    }

    @Override
    public void remove(final long loadBalancerId, final long instanceId, final String instanceIp, final Boolean revoke) {
        final SearchCriteria<LoadBalancerVMMapVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        sc.addAnd("instanceId", SearchCriteria.Op.IN, instanceId);
        sc.addAnd("instanceIp", SearchCriteria.Op.EQ, instanceIp);

        if (revoke != null) {
            sc.addAnd("revoke", SearchCriteria.Op.EQ, revoke);
        }

        expunge(sc);
    }
}
