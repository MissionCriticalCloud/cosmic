package com.cloud.network.dao;

import com.cloud.network.UserIpv6AddressVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserIpv6AddressDaoImpl extends GenericDaoBase<UserIpv6AddressVO, Long> implements UserIpv6AddressDao {
    private static final Logger s_logger = LoggerFactory.getLogger(IPAddressDaoImpl.class);

    protected final SearchBuilder<UserIpv6AddressVO> AllFieldsSearch;
    protected GenericSearchBuilder<UserIpv6AddressVO, Long> CountFreePublicIps;

    public UserIpv6AddressDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("dataCenterId", AllFieldsSearch.entity().getDataCenterId(), Op.EQ);
        AllFieldsSearch.and("ipAddress", AllFieldsSearch.entity().getAddress(), Op.EQ);
        AllFieldsSearch.and("vlan", AllFieldsSearch.entity().getVlanId(), Op.EQ);
        AllFieldsSearch.and("accountId", AllFieldsSearch.entity().getAccountId(), Op.EQ);
        AllFieldsSearch.and("network", AllFieldsSearch.entity().getNetworkId(), Op.EQ);
        AllFieldsSearch.and("physicalNetworkId", AllFieldsSearch.entity().getPhysicalNetworkId(), Op.EQ);
        AllFieldsSearch.done();

        CountFreePublicIps = createSearchBuilder(Long.class);
        CountFreePublicIps.select(null, Func.COUNT, null);
        CountFreePublicIps.and("networkId", CountFreePublicIps.entity().getSourceNetworkId(), SearchCriteria.Op.EQ);
        CountFreePublicIps.and("vlanId", CountFreePublicIps.entity().getVlanId(), SearchCriteria.Op.EQ);
        CountFreePublicIps.done();
    }

    @Override
    public List<UserIpv6AddressVO> listByAccount(final long accountId) {
        final SearchCriteria<UserIpv6AddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }

    @Override
    public List<UserIpv6AddressVO> listByVlanId(final long vlanId) {
        final SearchCriteria<UserIpv6AddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("vlan", vlanId);
        return listBy(sc);
    }

    @Override
    public List<UserIpv6AddressVO> listByDcId(final long dcId) {
        final SearchCriteria<UserIpv6AddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("dataCenterId", dcId);
        return listBy(sc);
    }

    @Override
    public List<UserIpv6AddressVO> listByNetwork(final long networkId) {
        final SearchCriteria<UserIpv6AddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        return listBy(sc);
    }

    @Override
    public UserIpv6AddressVO findByNetworkIdAndIp(final long networkId, final String ipAddress) {
        final SearchCriteria<UserIpv6AddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("networkId", networkId);
        sc.setParameters("ipAddress", ipAddress);
        return findOneBy(sc);
    }

    @Override
    public List<UserIpv6AddressVO> listByPhysicalNetworkId(final long physicalNetworkId) {
        final SearchCriteria<UserIpv6AddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);
        return listBy(sc);
    }

    @Override
    public long countExistedIpsInNetwork(final long networkId) {
        final SearchCriteria<Long> sc = CountFreePublicIps.create();
        sc.setParameters("networkId", networkId);
        return customSearch(sc, null).get(0);
    }

    @Override
    public long countExistedIpsInVlan(final long vlanId) {
        final SearchCriteria<Long> sc = CountFreePublicIps.create();
        sc.setParameters("vlanId", vlanId);
        return customSearch(sc, null).get(0);
    }
}
