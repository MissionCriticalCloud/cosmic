package com.cloud.network.dao;

import com.cloud.network.LBHealthCheckPolicyVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class LBHealthCheckPolicyDaoImpl extends GenericDaoBase<LBHealthCheckPolicyVO, Long> implements LBHealthCheckPolicyDao {

    @Override
    public void remove(final long loadBalancerId) {
        final SearchCriteria<LBHealthCheckPolicyVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);

        expunge(sc);
    }

    @Override
    public void remove(final long loadBalancerId, final Boolean revoke) {
        final SearchCriteria<LBHealthCheckPolicyVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        sc.addAnd("revoke", SearchCriteria.Op.EQ, revoke);

        expunge(sc);
    }

    @Override
    public List<LBHealthCheckPolicyVO> listByLoadBalancerIdAndDisplayFlag(final long loadBalancerId, final Boolean forDisplay) {
        final SearchCriteria<LBHealthCheckPolicyVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        if (forDisplay != null) {
            sc.addAnd("display", SearchCriteria.Op.EQ, forDisplay);
        }

        return listBy(sc);
    }

    @Override
    public List<LBHealthCheckPolicyVO> listByLoadBalancerId(final long loadBalancerId, final boolean pending) {
        final SearchCriteria<LBHealthCheckPolicyVO> sc = createSearchCriteria();
        sc.addAnd("loadBalancerId", SearchCriteria.Op.EQ, loadBalancerId);
        sc.addAnd("revoke", SearchCriteria.Op.EQ, pending);

        return listBy(sc);
    }
}
