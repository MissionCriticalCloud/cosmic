package com.cloud.network.dao;

import com.cloud.network.RemoteAccessVpn;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RemoteAccessVpnDaoImpl extends GenericDaoBase<RemoteAccessVpnVO, Long> implements RemoteAccessVpnDao {
    private static final Logger s_logger = LoggerFactory.getLogger(RemoteAccessVpnDaoImpl.class);

    private final SearchBuilder<RemoteAccessVpnVO> AllFieldsSearch;

    protected RemoteAccessVpnDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("accountId", AllFieldsSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("networkId", AllFieldsSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("vpcId", AllFieldsSearch.entity().getVpcId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("ipAddress", AllFieldsSearch.entity().getServerAddressId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    public RemoteAccessVpnVO findByPublicIpAddress(final long ipAddressId) {
        final SearchCriteria<RemoteAccessVpnVO> sc = AllFieldsSearch.create();
        sc.setParameters("ipAddress", ipAddressId);
        return findOneBy(sc);
    }

    @Override
    public RemoteAccessVpnVO findByPublicIpAddressAndState(final long ipAddressId, final RemoteAccessVpn.State state) {
        final SearchCriteria<RemoteAccessVpnVO> sc = AllFieldsSearch.create();
        sc.setParameters("ipAddress", ipAddressId);
        sc.setParameters("state", state);
        return findOneBy(sc);
    }

    @Override
    public RemoteAccessVpnVO findByAccountAndNetwork(final Long accountId, final Long networkId) {
        final SearchCriteria<RemoteAccessVpnVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("networkId", networkId);
        return findOneBy(sc);
    }

    @Override
    public RemoteAccessVpnVO findByAccountAndVpc(final Long accountId, final Long vpcId) {
        final SearchCriteria<RemoteAccessVpnVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("vpcId", vpcId);
        return findOneBy(sc);
    }

    @Override
    public List<RemoteAccessVpnVO> findByAccount(final Long accountId) {
        final SearchCriteria<RemoteAccessVpnVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }

    @Override
    public List<RemoteAccessVpnVO> listByNetworkId(final Long networkId) {
        final SearchCriteria<RemoteAccessVpnVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);
        return listBy(sc);
    }
}
