package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

public interface NetworkAccountDao extends GenericDao<NetworkAccountVO, Long> {
    NetworkAccountVO getAccountNetworkMapByNetworkId(long networkId);
}
