package com.cloud.vm.dao;

import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.vm.Nic;
import com.cloud.vm.Nic.State;
import com.cloud.vm.NicVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class NicDaoImpl extends GenericDaoBase<NicVO, Long> implements NicDao {
    @Inject
    VMInstanceDao _vmDao;
    private SearchBuilder<NicVO> AllFieldsSearch;
    private GenericSearchBuilder<NicVO, String> IpSearch;
    private SearchBuilder<NicVO> NonReleasedSearch;
    private GenericSearchBuilder<NicVO, Integer> deviceIdSearch;
    private GenericSearchBuilder<NicVO, Integer> CountByForStartingVms;

    public NicDaoImpl() {

    }

    @PostConstruct
    protected void init() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("instance", AllFieldsSearch.entity().getInstanceId(), Op.EQ);
        AllFieldsSearch.and("network", AllFieldsSearch.entity().getNetworkId(), Op.EQ);
        AllFieldsSearch.and("gateway", AllFieldsSearch.entity().getIPv4Gateway(), Op.EQ);
        AllFieldsSearch.and("vmType", AllFieldsSearch.entity().getVmType(), Op.EQ);
        AllFieldsSearch.and("address", AllFieldsSearch.entity().getIPv4Address(), Op.EQ);
        AllFieldsSearch.and("isDefault", AllFieldsSearch.entity().isDefaultNic(), Op.EQ);
        AllFieldsSearch.and("broadcastUri", AllFieldsSearch.entity().getBroadcastUri(), Op.EQ);
        AllFieldsSearch.and("secondaryip", AllFieldsSearch.entity().getSecondaryIp(), Op.EQ);
        AllFieldsSearch.and("nicid", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("strategy", AllFieldsSearch.entity().getReservationStrategy(), Op.EQ);
        AllFieldsSearch.done();

        IpSearch = createSearchBuilder(String.class);
        IpSearch.select(null, Func.DISTINCT, IpSearch.entity().getIPv4Address());
        IpSearch.and("network", IpSearch.entity().getNetworkId(), Op.EQ);
        IpSearch.and("address", IpSearch.entity().getIPv4Address(), Op.NNULL);
        IpSearch.done();

        NonReleasedSearch = createSearchBuilder();
        NonReleasedSearch.and("instance", NonReleasedSearch.entity().getInstanceId(), Op.EQ);
        NonReleasedSearch.and("network", NonReleasedSearch.entity().getNetworkId(), Op.EQ);
        NonReleasedSearch.and("state", NonReleasedSearch.entity().getState(), Op.NOTIN);
        NonReleasedSearch.done();

        deviceIdSearch = createSearchBuilder(Integer.class);
        deviceIdSearch.select(null, Func.DISTINCT, deviceIdSearch.entity().getDeviceId());
        deviceIdSearch.and("instance", deviceIdSearch.entity().getInstanceId(), Op.EQ);
        deviceIdSearch.done();

        CountByForStartingVms = createSearchBuilder(Integer.class);
        CountByForStartingVms.select(null, Func.COUNT, CountByForStartingVms.entity().getId());
        CountByForStartingVms.and("networkId", CountByForStartingVms.entity().getNetworkId(), Op.EQ);
        CountByForStartingVms.and("removed", CountByForStartingVms.entity().getRemoved(), Op.NULL);
        final SearchBuilder<VMInstanceVO> join1 = _vmDao.createSearchBuilder();
        join1.and("state", join1.entity().getState(), Op.EQ);
        CountByForStartingVms.join("vm", join1, CountByForStartingVms.entity().getInstanceId(), join1.entity().getId(), JoinBuilder.JoinType.INNER);
        CountByForStartingVms.done();
    }

    @Override
    public List<NicVO> listByVmId(final long instanceId) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("instance", instanceId);
        return listBy(sc);
    }

    @Override
    public List<String> listIpAddressInNetwork(final long networkId) {
        final SearchCriteria<String> sc = IpSearch.create();
        sc.setParameters("network", networkId);
        return customSearch(sc, null);
    }

    @Override
    public List<NicVO> listByVmIdIncludingRemoved(final long instanceId) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("instance", instanceId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<NicVO> listByNetworkId(final long networkId) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        return listBy(sc);
    }

    @Override
    public NicVO findByNtwkIdAndInstanceId(final long networkId, final long instanceId) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("instance", instanceId);
        return findOneBy(sc);
    }

    @Override
    public NicVO findByInstanceIdAndNetworkIdIncludingRemoved(final long networkId, final long instanceId) {
        final SearchCriteria<NicVO> sc = createSearchCriteria();
        sc.addAnd("networkId", SearchCriteria.Op.EQ, networkId);
        sc.addAnd("instanceId", SearchCriteria.Op.EQ, instanceId);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public NicVO findByNetworkIdTypeAndGateway(final long networkId, final VirtualMachine.Type vmType, final String gateway) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("vmType", vmType);
        sc.setParameters("gateway", gateway);
        return findOneBy(sc);
    }

    @Override
    public void removeNicsForInstance(final long instanceId) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("instance", instanceId);
        remove(sc);
    }

    @Override
    public NicVO findByNetworkIdAndType(final long networkId, final VirtualMachine.Type vmType) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("vmType", vmType);
        return findOneBy(sc);
    }

    @Override
    public NicVO findByIp4AddressAndNetworkId(final String ip4Address, final long networkId) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("address", ip4Address);
        sc.setParameters("network", networkId);
        return findOneBy(sc);
    }

    @Override
    public NicVO findDefaultNicForVM(final long instanceId) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("instance", instanceId);
        sc.setParameters("isDefault", 1);
        return findOneBy(sc);
    }

    @Override
    public NicVO findNonReleasedByInstanceIdAndNetworkId(final long networkId, final long instanceId) {
        final SearchCriteria<NicVO> sc = NonReleasedSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("instance", instanceId);
        sc.setParameters("state", State.Releasing, Nic.State.Deallocating);
        return findOneBy(sc);
    }

    @Override
    public String getIpAddress(final long networkId, final long instanceId) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("instance", instanceId);
        final NicVO nicVo = findOneBy(sc);
        if (nicVo != null) {
            return nicVo.getIPv4Address();
        }
        return null;
    }

    @Override
    public int getFreeDeviceId(final long instanceId) {
        final Filter searchFilter = new Filter(NicVO.class, "deviceId", true, null, null);
        final SearchCriteria<Integer> sc = deviceIdSearch.create();
        sc.setParameters("instance", instanceId);
        final List<Integer> deviceIds = customSearch(sc, searchFilter);

        int freeDeviceId = 0;
        for (final int deviceId : deviceIds) {
            if (deviceId > freeDeviceId) {
                break;
            }
            freeDeviceId++;
        }

        return freeDeviceId;
    }

    @Override
    public NicVO findByNetworkIdInstanceIdAndBroadcastUri(final long networkId, final long instanceId, final String broadcastUri) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("instance", instanceId);
        sc.setParameters("broadcastUri", broadcastUri);
        return findOneBy(sc);
    }

    @Override
    public NicVO findByIp4AddressAndNetworkIdAndInstanceId(final long networkId, final long instanceId, final String ip4Address) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("instance", instanceId);
        sc.setParameters("address", ip4Address);
        return findOneBy(sc);
    }

    @Override
    public List<NicVO> listByVmIdAndNicIdAndNtwkId(final long vmId, final Long nicId, final Long networkId) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("instance", vmId);

        if (nicId != null) {
            sc.setParameters("nicid", nicId);
        }

        if (networkId != null) {
            sc.setParameters("network", networkId);
        }
        return listBy(sc);
    }

    @Override
    public NicVO findByIp4AddressAndVmId(final String ip4Address, final long instance) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("address", ip4Address);
        sc.setParameters("instance", instance);
        return findOneBy(sc);
    }

    @Override
    public List<NicVO> listPlaceholderNicsByNetworkId(final long networkId) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("strategy", Nic.ReservationStrategy.PlaceHolder.toString());
        return listBy(sc);
    }

    @Override
    public List<NicVO> listPlaceholderNicsByNetworkIdAndVmType(final long networkId, final VirtualMachine.Type vmType) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("strategy", Nic.ReservationStrategy.PlaceHolder.toString());
        sc.setParameters("vmType", vmType);
        return listBy(sc);
    }

    @Override
    public NicVO findByInstanceIdAndIpAddressAndVmtype(final long instanceId, final String ipaddress, final VirtualMachine.Type type) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("instance", instanceId);
        sc.setParameters("address", ipaddress);
        sc.setParameters("vmType", type);
        return findOneBy(sc);
    }

    @Override
    public List<NicVO> listByNetworkIdTypeAndGatewayAndBroadcastUri(final long networkId, final VirtualMachine.Type vmType, final String gateway, final URI broadcasturi) {
        final SearchCriteria<NicVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("vmType", vmType);
        sc.setParameters("gateway", gateway);
        sc.setParameters("broadcastUri", broadcasturi);
        return listBy(sc);
    }

    @Override
    public int countNicsForStartingVms(final long networkId) {
        final SearchCriteria<Integer> sc = CountByForStartingVms.create();
        sc.setParameters("networkId", networkId);
        sc.setJoinParameters("vm", "state", VirtualMachine.State.Starting);
        final List<Integer> results = customSearch(sc, null);
        return results.get(0);
    }
}
