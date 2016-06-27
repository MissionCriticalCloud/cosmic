package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface RouterNetworkDao extends GenericDao<RouterNetworkVO, Long> {
    public List<Long> getRouterNetworks(long routerId);

    public RouterNetworkVO findByRouterAndNetwork(long routerId, long networkId);
}
