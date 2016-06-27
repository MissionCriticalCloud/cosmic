package com.cloud.network.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class LBStickinessPolicyDaoImpl extends GenericDaoBase<LBStickinessPolicyVO, Long> implements LBStickinessPolicyDao {

    @Override
    public void remove(final long loadBalancerId) {
        final SearchCriteria<LBStickinessPolicyVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);

        expunge(sc);
    }

    @Override
    public void remove(final long loadBalancerId, final Boolean revoke) {
        final SearchCriteria<LBStickinessPolicyVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        sc.addAnd("revoke", SearchCriteria.Op.EQ, revoke);

        expunge(sc);
    }

    @Override
    public List<LBStickinessPolicyVO> listByLoadBalancerIdAndDisplayFlag(final long loadBalancerId, final boolean forDisplay) {
        final SearchCriteria<LBStickinessPolicyVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        sc.addAnd("display", SearchCriteria.Op.EQ, forDisplay);

        return listBy(sc);
    }

    @Override
    public List<LBStickinessPolicyVO> listByLoadBalancerId(final long loadBalancerId, final boolean pending) {
        final SearchCriteria<LBStickinessPolicyVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        sc.addAnd("revoke", SearchCriteria.Op.EQ, pending);

        return listBy(sc);
    }
}
