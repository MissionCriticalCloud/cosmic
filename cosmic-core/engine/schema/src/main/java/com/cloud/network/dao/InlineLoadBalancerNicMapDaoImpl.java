package com.cloud.network.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchCriteria;

import org.springframework.stereotype.Component;

@Component
public class InlineLoadBalancerNicMapDaoImpl extends GenericDaoBase<InlineLoadBalancerNicMapVO, Long> implements InlineLoadBalancerNicMapDao {

    @Override
    public InlineLoadBalancerNicMapVO findByPublicIpAddress(final String publicIpAddress) {
        final SearchCriteria<InlineLoadBalancerNicMapVO> sc = createSearchCriteria();
        sc.addAnd("publicIpAddress", SearchCriteria.Op.EQ, publicIpAddress);

        return findOneBy(sc);
    }

    @Override
    public InlineLoadBalancerNicMapVO findByNicId(final long nicId) {
        final SearchCriteria<InlineLoadBalancerNicMapVO> sc = createSearchCriteria();
        sc.addAnd("nicId", SearchCriteria.Op.EQ, nicId);

        return findOneBy(sc);
    }
}
