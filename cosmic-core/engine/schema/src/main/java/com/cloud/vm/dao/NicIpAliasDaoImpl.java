package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.vm.NicIpAlias;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class NicIpAliasDaoImpl extends GenericDaoBase<NicIpAliasVO, Long> implements NicIpAliasDao {
    private final SearchBuilder<NicIpAliasVO> AllFieldsSearch;
    private final GenericSearchBuilder<NicIpAliasVO, String> IpSearch;

    protected NicIpAliasDaoImpl() {
        super();
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("instanceId", AllFieldsSearch.entity().getVmId(), Op.EQ);
        AllFieldsSearch.and("network", AllFieldsSearch.entity().getNetworkId(), Op.EQ);
        AllFieldsSearch.and("address", AllFieldsSearch.entity().getIp4Address(), Op.EQ);
        AllFieldsSearch.and("nicId", AllFieldsSearch.entity().getNicId(), Op.EQ);
        AllFieldsSearch.and("gateway", AllFieldsSearch.entity().getGateway(), Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.done();

        IpSearch = createSearchBuilder(String.class);
        IpSearch.select(null, Func.DISTINCT, IpSearch.entity().getIp4Address());
        IpSearch.and("network", IpSearch.entity().getNetworkId(), Op.EQ);
        IpSearch.and("address", IpSearch.entity().getIp4Address(), Op.NNULL);
        IpSearch.done();
    }

    @Override
    public List<NicIpAliasVO> listByVmId(final long instanceId) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", instanceId);
        return listBy(sc);
    }

    @Override
    public List<String> listAliasIpAddressInNetwork(final long networkId) {
        final SearchCriteria<String> sc = IpSearch.create();
        sc.setParameters("network", networkId);
        return customSearch(sc, null);
    }

    @Override
    public List<NicIpAliasVO> listByNetworkId(final long networkId) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        return listBy(sc);
    }

    @Override
    public NicIpAliasVO findByInstanceIdAndNetworkId(final long networkId, final long instanceId) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("instanceId", instanceId);
        sc.setParameters("state", NicIpAlias.State.active);
        return findOneBy(sc);
    }

    @Override
    public NicIpAliasVO findByIp4AddressAndNetworkId(final String ip4Address, final long networkId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<NicIpAliasVO> getAliasIpForVm(final long vmId) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", vmId);
        sc.setParameters("state", NicIpAlias.State.active);
        return listBy(sc);
    }

    @Override
    public List<NicIpAliasVO> listByNicId(final long nicId) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("nicId", nicId);
        return listBy(sc);
    }

    @Override
    public List<NicIpAliasVO> listByNicIdAndVmid(final long nicId, final long vmId) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("nicId", nicId);
        sc.setParameters("instanceId", vmId);
        return listBy(sc);
    }

    @Override
    public NicIpAliasVO findByIp4AddressAndNicId(final String ip4Address, final long nicId) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("address", ip4Address);
        sc.setParameters("nicId", nicId);
        return findOneBy(sc);
    }

    @Override
    public NicIpAliasVO findByIp4AddressAndNetworkIdAndInstanceId(final long networkId, final Long vmId, final String vmIp) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("instanceId", vmId);
        sc.setParameters("address", vmIp);
        return findOneBy(sc);
    }

    @Override
    public List<String> getAliasIpAddressesForNic(final long nicId) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("nicId", nicId);
        final List<NicIpAliasVO> results = search(sc, null);
        final List<String> ips = new ArrayList<>(results.size());
        for (final NicIpAliasVO result : results) {
            ips.add(result.getIp4Address());
        }
        return ips;
    }

    @Override
    public Integer countAliasIps(final long id) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", id);
        final List<NicIpAliasVO> list = listBy(sc);
        return list.size();
    }

    @Override
    public NicIpAliasVO findByIp4AddressAndVmId(final String ip4Address, final long vmId) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("address", ip4Address);
        sc.setParameters("instanceId", vmId);
        return findOneBy(sc);
    }

    @Override
    public NicIpAliasVO findByGatewayAndNetworkIdAndState(final String gateway, final long networkId, final NicIpAlias.State state) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("gateway", gateway);
        sc.setParameters("network", networkId);
        sc.setParameters("state", state);
        return findOneBy(sc);
    }

    @Override
    public List<NicIpAliasVO> listByNetworkIdAndState(final long networkId, final NicIpAlias.State state) {
        final SearchCriteria<NicIpAliasVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("state", state);
        return listBy(sc);
    }
}
