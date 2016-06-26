package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

public interface InlineLoadBalancerNicMapDao extends GenericDao<InlineLoadBalancerNicMapVO, Long> {
    InlineLoadBalancerNicMapVO findByPublicIpAddress(String publicIpAddress);

    InlineLoadBalancerNicMapVO findByNicId(long nicId);
}
