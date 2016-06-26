package com.cloud.dc.dao;

import com.cloud.dc.AccountVlanMapVO;
import com.cloud.dc.DomainVlanMapVO;
import com.cloud.dc.PodVlanMapVO;
import com.cloud.dc.Vlan;
import com.cloud.dc.Vlan.VlanType;
import com.cloud.dc.VlanVO;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.utils.Pair;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class VlanDaoImpl extends GenericDaoBase<VlanVO, Long> implements VlanDao {

    private final String FindZoneWideVlans =
            "SELECT * FROM vlan WHERE data_center_id=? and vlan_type=? and vlan_id!=? and id not in (select vlan_db_id from account_vlan_map)";

    protected SearchBuilder<VlanVO> ZoneVlanIdSearch;
    protected SearchBuilder<VlanVO> ZoneSearch;
    protected SearchBuilder<VlanVO> ZoneTypeSearch;
    protected SearchBuilder<VlanVO> ZoneTypeAllPodsSearch;
    protected SearchBuilder<VlanVO> ZoneTypePodSearch;
    protected SearchBuilder<VlanVO> ZoneVlanSearch;
    protected SearchBuilder<VlanVO> NetworkVlanSearch;
    protected SearchBuilder<VlanVO> PhysicalNetworkVlanSearch;
    protected SearchBuilder<VlanVO> ZoneWideNonDedicatedVlanSearch;
    protected SearchBuilder<VlanVO> VlanGatewaysearch;
    protected SearchBuilder<VlanVO> DedicatedVlanSearch;

    protected SearchBuilder<AccountVlanMapVO> AccountVlanMapSearch;
    protected SearchBuilder<DomainVlanMapVO> DomainVlanMapSearch;

    @Inject
    protected PodVlanMapDao _podVlanMapDao;
    @Inject
    protected AccountVlanMapDao _accountVlanMapDao;
    @Inject
    protected DomainVlanMapDao _domainVlanMapDao;
    @Inject
    protected IPAddressDao _ipAddressDao;

    public VlanDaoImpl() {
        ZoneVlanIdSearch = createSearchBuilder();
        ZoneVlanIdSearch.and("zoneId", ZoneVlanIdSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        ZoneVlanIdSearch.and("vlanId", ZoneVlanIdSearch.entity().getVlanTag(), SearchCriteria.Op.EQ);
        ZoneVlanIdSearch.done();

        ZoneSearch = createSearchBuilder();
        ZoneSearch.and("zoneId", ZoneSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        ZoneSearch.done();

        ZoneTypeSearch = createSearchBuilder();
        ZoneTypeSearch.and("zoneId", ZoneTypeSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        ZoneTypeSearch.and("vlanType", ZoneTypeSearch.entity().getVlanType(), SearchCriteria.Op.EQ);
        ZoneTypeSearch.done();

        NetworkVlanSearch = createSearchBuilder();
        NetworkVlanSearch.and("networkOfferingId", NetworkVlanSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        NetworkVlanSearch.done();

        PhysicalNetworkVlanSearch = createSearchBuilder();
        PhysicalNetworkVlanSearch.and("physicalNetworkId", PhysicalNetworkVlanSearch.entity().getPhysicalNetworkId(), SearchCriteria.Op.EQ);
        PhysicalNetworkVlanSearch.done();

        VlanGatewaysearch = createSearchBuilder();
        VlanGatewaysearch.and("gateway", VlanGatewaysearch.entity().getVlanGateway(), SearchCriteria.Op.EQ);
        VlanGatewaysearch.and("networkid", VlanGatewaysearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        VlanGatewaysearch.done();
    }

    @Override
    public VlanVO findByZoneAndVlanId(final long zoneId, final String vlanId) {
        final SearchCriteria<VlanVO> sc = ZoneVlanIdSearch.create();
        sc.setParameters("zoneId", zoneId);
        sc.setParameters("vlanId", vlanId);
        return findOneBy(sc);
    }

    @Override
    public List<VlanVO> listByZone(final long zoneId) {
        final SearchCriteria<VlanVO> sc = ZoneSearch.create();
        sc.setParameters("zoneId", zoneId);
        return listBy(sc);
    }

    @Override
    public List<VlanVO> listByType(final VlanType vlanType) {
        final SearchCriteria<VlanVO> sc = ZoneTypeSearch.create();
        sc.setParameters("vlanType", vlanType);
        return listBy(sc);
    }

    @Override
    public List<VlanVO> listByZoneAndType(final long zoneId, final VlanType vlanType) {
        final SearchCriteria<VlanVO> sc = ZoneTypeSearch.create();
        sc.setParameters("zoneId", zoneId);
        sc.setParameters("vlanType", vlanType);
        return listBy(sc);
    }

    @Override
    public List<VlanVO> listVlansForPod(final long podId) {
        //FIXME: use a join statement to improve the performance (should be minor since we expect only one or two
        final List<PodVlanMapVO> vlanMaps = _podVlanMapDao.listPodVlanMapsByPod(podId);
        final List<VlanVO> result = new ArrayList<>();
        for (final PodVlanMapVO pvmvo : vlanMaps) {
            result.add(findById(pvmvo.getVlanDbId()));
        }
        return result;
    }

    @Override
    public List<VlanVO> listVlansForPodByType(final long podId, final VlanType vlanType) {
        //FIXME: use a join statement to improve the performance (should be minor since we expect only one or two)
        final List<PodVlanMapVO> vlanMaps = _podVlanMapDao.listPodVlanMapsByPod(podId);
        final List<VlanVO> result = new ArrayList<>();
        for (final PodVlanMapVO pvmvo : vlanMaps) {
            final VlanVO vlan = findById(pvmvo.getVlanDbId());
            if (vlan.getVlanType() == vlanType) {
                result.add(vlan);
            }
        }
        return result;
    }

    @Override
    public void addToPod(final long podId, final long vlanDbId) {
        final PodVlanMapVO pvmvo = new PodVlanMapVO(podId, vlanDbId);
        _podVlanMapDao.persist(pvmvo);
    }

    @Override
    public List<VlanVO> listVlansForAccountByType(final Long zoneId, final long accountId, final VlanType vlanType) {
        //FIXME: use a join statement to improve the performance (should be minor since we expect only one or two)
        final List<AccountVlanMapVO> vlanMaps = _accountVlanMapDao.listAccountVlanMapsByAccount(accountId);
        final List<VlanVO> result = new ArrayList<>();
        for (final AccountVlanMapVO acvmvo : vlanMaps) {
            final VlanVO vlan = findById(acvmvo.getVlanDbId());
            if (vlan.getVlanType() == vlanType && (zoneId == null || vlan.getDataCenterId() == zoneId)) {
                result.add(vlan);
            }
        }
        return result;
    }

    @Override
    public boolean zoneHasDirectAttachUntaggedVlans(final long zoneId) {
        final SearchCriteria<VlanVO> sc = ZoneTypeAllPodsSearch.create();
        sc.setParameters("zoneId", zoneId);
        sc.setParameters("vlanType", VlanType.DirectAttached);

        return listIncludingRemovedBy(sc).size() > 0;
    }

    @Override
    public List<VlanVO> listZoneWideVlans(final long zoneId, final VlanType vlanType, final String vlanId) {
        final SearchCriteria<VlanVO> sc = ZoneVlanSearch.create();
        sc.setParameters("zoneId", zoneId);
        sc.setParameters("vlanId", vlanId);
        sc.setParameters("vlanType", vlanType);
        return listBy(sc);
    }

    @Override
    @DB
    public List<VlanVO> searchForZoneWideVlans(final long dcId, final String vlanType, final String vlanId) {
        final StringBuilder sql = new StringBuilder(FindZoneWideVlans);
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        final List<VlanVO> zoneWideVlans = new ArrayList<>();
        try (PreparedStatement pstmt = txn.prepareStatement(sql.toString())) {
            if (pstmt != null) {
                pstmt.setLong(1, dcId);
                pstmt.setString(2, vlanType);
                pstmt.setString(3, vlanId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        zoneWideVlans.add(toEntityBean(rs, false));
                    }
                } catch (final SQLException e) {
                    throw new CloudRuntimeException("searchForZoneWideVlans:Exception:" + e.getMessage(), e);
                }
            }
            return zoneWideVlans;
        } catch (final SQLException e) {
            throw new CloudRuntimeException("searchForZoneWideVlans:Exception:" + e.getMessage(), e);
        }
    }

    @Override
    public List<VlanVO> listVlansByNetworkId(final long networkOfferingId) {
        final SearchCriteria<VlanVO> sc = NetworkVlanSearch.create();
        sc.setParameters("networkOfferingId", networkOfferingId);
        return listBy(sc);
    }

    @Override
    public List<VlanVO> listVlansByPhysicalNetworkId(final long physicalNetworkId) {
        final SearchCriteria<VlanVO> sc = PhysicalNetworkVlanSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);
        return listBy(sc);
    }

    @Override
    public List<VlanVO> listZoneWideNonDedicatedVlans(final long zoneId) {
        final SearchCriteria<VlanVO> sc = ZoneWideNonDedicatedVlanSearch.create();
        sc.setParameters("zoneId", zoneId);
        return listBy(sc);
    }

    @Override
    public List<VlanVO> listVlansByNetworkIdAndGateway(final long networkid, final String gateway) {
        final SearchCriteria<VlanVO> sc = VlanGatewaysearch.create();
        sc.setParameters("networkid", networkid);
        sc.setParameters("gateway", gateway);
        return listBy(sc);
    }

    @Override
    public List<VlanVO> listDedicatedVlans(final long accountId) {
        final SearchCriteria<VlanVO> sc = DedicatedVlanSearch.create();
        sc.setJoinParameters("AccountVlanMapSearch", "accountId", accountId);
        return listBy(sc);
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        final boolean result = super.configure(name, params);
        ZoneTypeAllPodsSearch = createSearchBuilder();
        ZoneTypeAllPodsSearch.and("zoneId", ZoneTypeAllPodsSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        ZoneTypeAllPodsSearch.and("vlanType", ZoneTypeAllPodsSearch.entity().getVlanType(), SearchCriteria.Op.EQ);

        final SearchBuilder<PodVlanMapVO> PodVlanSearch = _podVlanMapDao.createSearchBuilder();
        PodVlanSearch.and("podId", PodVlanSearch.entity().getPodId(), SearchCriteria.Op.NNULL);
        ZoneTypeAllPodsSearch.join("vlan", PodVlanSearch, PodVlanSearch.entity().getVlanDbId(), ZoneTypeAllPodsSearch.entity().getId(), JoinBuilder.JoinType.INNER);

        ZoneTypeAllPodsSearch.done();
        PodVlanSearch.done();

        ZoneTypePodSearch = createSearchBuilder();
        ZoneTypePodSearch.and("zoneId", ZoneTypePodSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        ZoneTypePodSearch.and("vlanType", ZoneTypePodSearch.entity().getVlanType(), SearchCriteria.Op.EQ);

        final SearchBuilder<PodVlanMapVO> PodVlanSearch2 = _podVlanMapDao.createSearchBuilder();
        PodVlanSearch2.and("podId", PodVlanSearch2.entity().getPodId(), SearchCriteria.Op.EQ);
        ZoneTypePodSearch.join("vlan", PodVlanSearch2, PodVlanSearch2.entity().getVlanDbId(), ZoneTypePodSearch.entity().getId(), JoinBuilder.JoinType.INNER);
        PodVlanSearch2.done();
        ZoneTypePodSearch.done();

        ZoneWideNonDedicatedVlanSearch = createSearchBuilder();
        ZoneWideNonDedicatedVlanSearch.and("zoneId", ZoneWideNonDedicatedVlanSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        AccountVlanMapSearch = _accountVlanMapDao.createSearchBuilder();
        AccountVlanMapSearch.and("accountId", AccountVlanMapSearch.entity().getAccountId(), SearchCriteria.Op.NULL);
        ZoneWideNonDedicatedVlanSearch.join("AccountVlanMapSearch", AccountVlanMapSearch, ZoneWideNonDedicatedVlanSearch.entity().getId(), AccountVlanMapSearch.entity()
                                                                                                                                                               .getVlanDbId(),
                JoinBuilder.JoinType.LEFTOUTER);
        DomainVlanMapSearch = _domainVlanMapDao.createSearchBuilder();
        DomainVlanMapSearch.and("domainId", DomainVlanMapSearch.entity().getDomainId(), SearchCriteria.Op.NULL);
        ZoneWideNonDedicatedVlanSearch.join("DomainVlanMapSearch", DomainVlanMapSearch, ZoneWideNonDedicatedVlanSearch.entity().getId(), DomainVlanMapSearch.entity().getVlanDbId
                (), JoinBuilder.JoinType.LEFTOUTER);
        ZoneWideNonDedicatedVlanSearch.done();
        AccountVlanMapSearch.done();
        DomainVlanMapSearch.done();

        DedicatedVlanSearch = createSearchBuilder();
        AccountVlanMapSearch = _accountVlanMapDao.createSearchBuilder();
        AccountVlanMapSearch.and("accountId", AccountVlanMapSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        DedicatedVlanSearch.join("AccountVlanMapSearch", AccountVlanMapSearch, DedicatedVlanSearch.entity().getId(), AccountVlanMapSearch.entity().getVlanDbId(),
                JoinBuilder.JoinType.LEFTOUTER);
        DedicatedVlanSearch.done();
        AccountVlanMapSearch.done();

        return result;
    }

    private VlanVO findNextVlan(final long zoneId, final Vlan.VlanType vlanType) {
        final List<VlanVO> allVlans = listByZoneAndType(zoneId, vlanType);
        final List<VlanVO> emptyVlans = new ArrayList<>();
        final List<VlanVO> fullVlans = new ArrayList<>();

        // Try to find a VLAN that is partially allocated
        for (final VlanVO vlan : allVlans) {
            final long vlanDbId = vlan.getId();

            final int countOfAllocatedIps = _ipAddressDao.countIPs(zoneId, vlanDbId, true);
            final int countOfAllIps = _ipAddressDao.countIPs(zoneId, vlanDbId, false);

            if ((countOfAllocatedIps > 0) && (countOfAllocatedIps < countOfAllIps)) {
                return vlan;
            } else if (countOfAllocatedIps == 0) {
                emptyVlans.add(vlan);
            } else if (countOfAllocatedIps == countOfAllIps) {
                fullVlans.add(vlan);
            }
        }

        if (emptyVlans.isEmpty()) {
            return null;
        }

        // Try to find an empty VLAN with the same tag/subnet as a VLAN that is full
        for (final VlanVO fullVlan : fullVlans) {
            for (final VlanVO emptyVlan : emptyVlans) {
                if (fullVlan.getVlanTag().equals(emptyVlan.getVlanTag()) && fullVlan.getVlanGateway().equals(emptyVlan.getVlanGateway()) &&
                        fullVlan.getVlanNetmask().equals(emptyVlan.getVlanNetmask())) {
                    return emptyVlan;
                }
            }
        }

        // Return a random empty VLAN
        return emptyVlans.get(0);
    }

    public Pair<String, VlanVO> assignPodDirectAttachIpAddress(final long zoneId, final long podId, final long accountId, final long domainId) {
        final SearchCriteria<VlanVO> sc = ZoneTypePodSearch.create();
        sc.setParameters("zoneId", zoneId);
        sc.setParameters("vlanType", VlanType.DirectAttached);
        sc.setJoinParameters("vlan", "podId", podId);

        final VlanVO vlan = findOneIncludingRemovedBy(sc);
        if (vlan == null) {
            return null;
        }

        return null;
        //        String ipAddress = _ipAddressDao.assignIpAddress(accountId, domainId, vlan.getId(), false).getAddress();
        //        if (ipAddress == null) {
        //            return null;
        //        }
        //        return new Pair<String, VlanVO>(ipAddress, vlan);

    }
}
