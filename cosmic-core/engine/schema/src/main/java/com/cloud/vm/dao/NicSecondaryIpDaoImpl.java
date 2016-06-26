package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class NicSecondaryIpDaoImpl extends GenericDaoBase<NicSecondaryIpVO, Long> implements NicSecondaryIpDao {
    private final SearchBuilder<NicSecondaryIpVO> AllFieldsSearch;
    private final GenericSearchBuilder<NicSecondaryIpVO, String> IpSearch;
    protected GenericSearchBuilder<NicSecondaryIpVO, Long> CountByNicId;

    protected NicSecondaryIpDaoImpl() {
        super();
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("instanceId", AllFieldsSearch.entity().getVmId(), Op.EQ);
        AllFieldsSearch.and("network", AllFieldsSearch.entity().getNetworkId(), Op.EQ);
        AllFieldsSearch.and("address", AllFieldsSearch.entity().getIp4Address(), Op.EQ);
        AllFieldsSearch.and("nicId", AllFieldsSearch.entity().getNicId(), Op.EQ);
        AllFieldsSearch.done();

        IpSearch = createSearchBuilder(String.class);
        IpSearch.select(null, Func.DISTINCT, IpSearch.entity().getIp4Address());
        IpSearch.and("network", IpSearch.entity().getNetworkId(), Op.EQ);
        IpSearch.and("address", IpSearch.entity().getIp4Address(), Op.NNULL);
        IpSearch.done();

        CountByNicId = createSearchBuilder(Long.class);
        CountByNicId.select(null, Func.COUNT, null);
        CountByNicId.and("nic", CountByNicId.entity().getNicId(), SearchCriteria.Op.EQ);
        CountByNicId.done();
    }

    @Override
    public List<NicSecondaryIpVO> listByVmId(final long instanceId) {
        final SearchCriteria<NicSecondaryIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", instanceId);
        return listBy(sc);
    }

    @Override
    public List<String> listSecondaryIpAddressInNetwork(final long networkId) {
        final SearchCriteria<String> sc = IpSearch.create();
        sc.setParameters("network", networkId);
        return customSearch(sc, null);
    }

    @Override
    public List<NicSecondaryIpVO> listByNetworkId(final long networkId) {
        final SearchCriteria<NicSecondaryIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        return listBy(sc);
    }

    @Override
    public NicSecondaryIpVO findByInstanceIdAndNetworkId(final long networkId, final long instanceId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NicSecondaryIpVO findByIp4AddressAndNetworkId(final String ip4Address, final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<NicSecondaryIpVO> getSecondaryIpAddressesForVm(final long vmId) {
        final SearchCriteria<NicSecondaryIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", vmId);
        return listBy(sc);
    }

    @Override
    public List<NicSecondaryIpVO> listByNicId(final long nicId) {
        final SearchCriteria<NicSecondaryIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("nicId", nicId);
        return listBy(sc);
    }

    @Override
    public List<NicSecondaryIpVO> listByNicIdAndVmid(final long nicId, final long vmId) {
        final SearchCriteria<NicSecondaryIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("nicId", nicId);
        sc.setParameters("instanceId", vmId);
        return listBy(sc);
    }

    @Override
    public NicSecondaryIpVO findByIp4AddressAndNicId(final String ip4Address, final long nicId) {
        final SearchCriteria<NicSecondaryIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("address", ip4Address);
        sc.setParameters("nicId", nicId);
        return findOneBy(sc);
    }

    @Override
    public NicSecondaryIpVO findByIp4AddressAndNetworkIdAndInstanceId(final long networkId, final Long vmId, final String vmIp) {
        final SearchCriteria<NicSecondaryIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("instanceId", vmId);
        sc.setParameters("address", vmIp);
        return findOneBy(sc);
    }

    @Override
    public List<String> getSecondaryIpAddressesForNic(final long nicId) {
        final SearchCriteria<NicSecondaryIpVO> sc = AllFieldsSearch.create();
        sc.setParameters("nicId", nicId);
        final List<NicSecondaryIpVO> results = search(sc, null);
        final List<String> ips = new ArrayList<>(results.size());
        for (final NicSecondaryIpVO result : results) {
            ips.add(result.getIp4Address());
        }
        return ips;
    }

    @Override
    public Long countByNicId(final long nicId) {
        final SearchCriteria<Long> sc = CountByNicId.create();
        sc.setParameters("nic", nicId);
        return customSearch(sc, null).get(0);
    }
}
