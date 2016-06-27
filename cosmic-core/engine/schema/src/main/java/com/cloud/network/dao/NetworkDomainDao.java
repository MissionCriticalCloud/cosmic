package com.cloud.network.dao;

import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface NetworkDomainDao extends GenericDao<NetworkDomainVO, Long> {
    List<NetworkDomainVO> listDomainNetworkMapByDomain(Object... domainId);

    NetworkDomainVO getDomainNetworkMapByNetworkId(long networkId);

    List<Long> listNetworkIdsByDomain(long domainId);
}
