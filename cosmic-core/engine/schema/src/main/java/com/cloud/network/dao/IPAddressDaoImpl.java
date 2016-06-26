package com.cloud.network.dao;

import com.cloud.dc.Vlan.VlanType;
import com.cloud.dc.VlanVO;
import com.cloud.dc.dao.VlanDao;
import com.cloud.network.IpAddress.State;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.net.Ip;
import org.apache.cloudstack.resourcedetail.dao.UserIpAddressDetailsDao;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@DB
public class IPAddressDaoImpl extends GenericDaoBase<IPAddressVO, Long> implements IPAddressDao {
    private static final Logger s_logger = LoggerFactory.getLogger(IPAddressDaoImpl.class);

    protected SearchBuilder<IPAddressVO> AllFieldsSearch;
    protected SearchBuilder<IPAddressVO> VlanDbIdSearchUnallocated;
    protected GenericSearchBuilder<IPAddressVO, Integer> AllIpCount;
    protected GenericSearchBuilder<IPAddressVO, Integer> AllIpCountForDc;
    protected GenericSearchBuilder<IPAddressVO, Integer> AllocatedIpCount;
    protected GenericSearchBuilder<IPAddressVO, Integer> AllocatedIpCountForDc;
    protected GenericSearchBuilder<IPAddressVO, Integer> AllIpCountForDashboard;
    protected SearchBuilder<IPAddressVO> DeleteAllExceptGivenIp;
    protected GenericSearchBuilder<IPAddressVO, Long> AllocatedIpCountForAccount;
    @Inject
    protected VlanDao _vlanDao;
    protected GenericSearchBuilder<IPAddressVO, Long> CountFreePublicIps;
    @Inject
    ResourceTagDao _tagsDao;
    @Inject
    UserIpAddressDetailsDao _detailsDao;

    // make it public for JUnit test
    public IPAddressDaoImpl() {
    }

    @PostConstruct
    public void init() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("dataCenterId", AllFieldsSearch.entity().getDataCenterId(), Op.EQ);
        AllFieldsSearch.and("ipAddress", AllFieldsSearch.entity().getAddress(), Op.EQ);
        AllFieldsSearch.and("vlan", AllFieldsSearch.entity().getVlanId(), Op.EQ);
        AllFieldsSearch.and("accountId", AllFieldsSearch.entity().getAllocatedToAccountId(), Op.EQ);
        AllFieldsSearch.and("sourceNat", AllFieldsSearch.entity().isSourceNat(), Op.EQ);
        AllFieldsSearch.and("network", AllFieldsSearch.entity().getAssociatedWithNetworkId(), Op.EQ);
        AllFieldsSearch.and("associatedWithVmId", AllFieldsSearch.entity().getAssociatedWithVmId(), Op.EQ);
        AllFieldsSearch.and("oneToOneNat", AllFieldsSearch.entity().isOneToOneNat(), Op.EQ);
        AllFieldsSearch.and("sourcenetwork", AllFieldsSearch.entity().getSourceNetworkId(), Op.EQ);
        AllFieldsSearch.and("physicalNetworkId", AllFieldsSearch.entity().getPhysicalNetworkId(), Op.EQ);
        AllFieldsSearch.and("vpcId", AllFieldsSearch.entity().getVpcId(), Op.EQ);
        AllFieldsSearch.and("associatedVmIp", AllFieldsSearch.entity().getVmIp(), Op.EQ);
        AllFieldsSearch.done();

        VlanDbIdSearchUnallocated = createSearchBuilder();
        VlanDbIdSearchUnallocated.and("allocated", VlanDbIdSearchUnallocated.entity().getAllocatedTime(), Op.NULL);
        VlanDbIdSearchUnallocated.and("vlanDbId", VlanDbIdSearchUnallocated.entity().getVlanId(), Op.EQ);
        VlanDbIdSearchUnallocated.done();

        AllIpCount = createSearchBuilder(Integer.class);
        AllIpCount.select(null, Func.COUNT, AllIpCount.entity().getAddress());
        AllIpCount.and("dc", AllIpCount.entity().getDataCenterId(), Op.EQ);
        AllIpCount.and("vlan", AllIpCount.entity().getVlanId(), Op.EQ);
        AllIpCount.done();

        AllIpCountForDc = createSearchBuilder(Integer.class);
        AllIpCountForDc.select(null, Func.COUNT, AllIpCountForDc.entity().getAddress());
        AllIpCountForDc.and("dc", AllIpCountForDc.entity().getDataCenterId(), Op.EQ);
        AllIpCountForDc.done();

        AllocatedIpCount = createSearchBuilder(Integer.class);
        AllocatedIpCount.select(null, Func.COUNT, AllocatedIpCount.entity().getAddress());
        AllocatedIpCount.and("dc", AllocatedIpCount.entity().getDataCenterId(), Op.EQ);
        AllocatedIpCount.and("vlan", AllocatedIpCount.entity().getVlanId(), Op.EQ);
        AllocatedIpCount.and("allocated", AllocatedIpCount.entity().getAllocatedTime(), Op.NNULL);
        AllocatedIpCount.done();

        AllocatedIpCountForDc = createSearchBuilder(Integer.class);
        AllocatedIpCountForDc.select(null, Func.COUNT, AllocatedIpCountForDc.entity().getAddress());
        AllocatedIpCountForDc.and("dc", AllocatedIpCountForDc.entity().getDataCenterId(), Op.EQ);
        AllocatedIpCountForDc.and("allocated", AllocatedIpCountForDc.entity().getAllocatedTime(), Op.NNULL);
        AllocatedIpCountForDc.done();

        AllIpCountForDashboard = createSearchBuilder(Integer.class);
        AllIpCountForDashboard.select(null, Func.COUNT, AllIpCountForDashboard.entity().getAddress());
        AllIpCountForDashboard.and("dc", AllIpCountForDashboard.entity().getDataCenterId(), Op.EQ);
        AllIpCountForDashboard.and("state", AllIpCountForDashboard.entity().getState(), SearchCriteria.Op.NEQ);

        final SearchBuilder<VlanVO> virtaulNetworkVlan = _vlanDao.createSearchBuilder();
        virtaulNetworkVlan.and("vlanType", virtaulNetworkVlan.entity().getVlanType(), SearchCriteria.Op.EQ);

        AllIpCountForDashboard.join("vlan", virtaulNetworkVlan, virtaulNetworkVlan.entity().getId(), AllIpCountForDashboard.entity().getVlanId(),
                JoinBuilder.JoinType.INNER);
        virtaulNetworkVlan.done();
        AllIpCountForDashboard.done();

        AllocatedIpCountForAccount = createSearchBuilder(Long.class);
        AllocatedIpCountForAccount.select(null, Func.COUNT, AllocatedIpCountForAccount.entity().getAddress());
        AllocatedIpCountForAccount.and("account", AllocatedIpCountForAccount.entity().getAllocatedToAccountId(), Op.EQ);
        AllocatedIpCountForAccount.and("allocated", AllocatedIpCountForAccount.entity().getAllocatedTime(), Op.NNULL);
        AllocatedIpCountForAccount.and("network", AllocatedIpCountForAccount.entity().getAssociatedWithNetworkId(), Op.NNULL);
        AllocatedIpCountForAccount.done();

        CountFreePublicIps = createSearchBuilder(Long.class);
        CountFreePublicIps.select(null, Func.COUNT, null);
        CountFreePublicIps.and("state", CountFreePublicIps.entity().getState(), SearchCriteria.Op.EQ);
        CountFreePublicIps.and("networkId", CountFreePublicIps.entity().getSourceNetworkId(), SearchCriteria.Op.EQ);
        final SearchBuilder<VlanVO> join = _vlanDao.createSearchBuilder();
        join.and("vlanType", join.entity().getVlanType(), Op.EQ);
        CountFreePublicIps.join("vlans", join, CountFreePublicIps.entity().getVlanId(), join.entity().getId(), JoinBuilder.JoinType.INNER);
        CountFreePublicIps.done();

        DeleteAllExceptGivenIp = createSearchBuilder();
        DeleteAllExceptGivenIp.and("vlanDbId", DeleteAllExceptGivenIp.entity().getVlanId(), Op.EQ);
        DeleteAllExceptGivenIp.and("ip", DeleteAllExceptGivenIp.entity().getAddress(), Op.NEQ);
    }

    @Override
    @DB
    public IPAddressVO markAsUnavailable(final long ipAddressId) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", ipAddressId);

        final IPAddressVO ip = createForUpdate();
        ip.setState(State.Releasing);
        if (update(ip, sc) != 1) {
            return null;
        }

        return findOneBy(sc);
    }

    @Override
    public void unassignIpAddress(final long ipAddressId) {
        final IPAddressVO address = createForUpdate();
        address.setAllocatedToAccountId(null);
        address.setAllocatedInDomainId(null);
        address.setAllocatedTime(null);
        address.setSourceNat(false);
        address.setOneToOneNat(false);
        address.setAssociatedWithVmId(null);
        address.setState(State.Free);
        address.setAssociatedWithNetworkId(null);
        address.setVpcId(null);
        address.setSystem(false);
        address.setVmIp(null);
        address.setDisplay(true);
        //remove resource details for the ip
        _detailsDao.removeDetails(ipAddressId);
        update(ipAddressId, address);
    }

    @Override
    public List<IPAddressVO> listByAccount(final long accountId) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }

    @Override
    public List<IPAddressVO> listByVlanId(final long vlanId) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("vlan", vlanId);
        return listBy(sc);
    }

    @Override
    public List<IPAddressVO> listByDcIdIpAddress(final long dcId, final String ipAddress) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("dataCenterId", dcId);
        sc.setParameters("ipAddress", ipAddress);
        return listBy(sc);
    }

    @Override
    public List<IPAddressVO> listByDcId(final long dcId) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("dataCenterId", dcId);
        return listBy(sc);
    }

    @Override
    public List<IPAddressVO> listByAssociatedNetwork(final long networkId, final Boolean isSourceNat) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);

        if (isSourceNat != null) {
            sc.setParameters("sourceNat", isSourceNat);
        }

        return listBy(sc);
    }

    @Override
    public List<IPAddressVO> listStaticNatPublicIps(final long networkId) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("oneToOneNat", true);
        return listBy(sc);
    }

    @Override
    public int countIPs(final long dcId, final boolean onlyCountAllocated) {
        final SearchCriteria<Integer> sc = onlyCountAllocated ? AllocatedIpCountForDc.create() : AllIpCountForDc.create();
        sc.setParameters("dc", dcId);

        return customSearch(sc, null).get(0);
    }

    @Override
    public int countIPs(final long dcId, final long vlanId, final boolean onlyCountAllocated) {
        final SearchCriteria<Integer> sc = onlyCountAllocated ? AllocatedIpCount.create() : AllIpCount.create();
        sc.setParameters("dc", dcId);
        sc.setParameters("vlan", vlanId);

        return customSearch(sc, null).get(0);
    }

    @Override
    @DB
    public int countIPs(final long dcId, final Long accountId, final String vlanId, final String vlanGateway, final String vlanNetmask) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        int ipCount = 0;
        try {
            final String sql =
                    "SELECT count(*) FROM user_ip_address u INNER JOIN vlan v on (u.vlan_db_id = v.id AND v.data_center_id = ? AND v.vlan_id = ? AND v.vlan_gateway = ? AND v" +
                            ".vlan_netmask = ? AND u.account_id = ?)";

            final PreparedStatement pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, dcId);
            pstmt.setString(2, vlanId);
            pstmt.setString(3, vlanGateway);
            pstmt.setString(4, vlanNetmask);
            pstmt.setLong(5, accountId);
            final ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                ipCount = rs.getInt(1);
            }
        } catch (final Exception e) {
            s_logger.warn("Exception counting IP addresses", e);
        }

        return ipCount;
    }

    @Override
    public long countAllocatedIPsForAccount(final long accountId) {
        final SearchCriteria<Long> sc = AllocatedIpCountForAccount.create();
        sc.setParameters("account", accountId);
        return customSearch(sc, null).get(0);
    }

    @Override
    public boolean mark(final long dcId, final Ip ip) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("dataCenterId", dcId);
        sc.setParameters("ipAddress", ip);

        final IPAddressVO vo = createForUpdate();
        vo.setAllocatedTime(new Date());
        vo.setState(State.Allocated);

        return update(vo, sc) >= 1;
    }

    @Override
    public int countIPsForNetwork(final long dcId, final boolean onlyCountAllocated, final VlanType vlanType) {
        final SearchCriteria<Integer> sc = AllIpCountForDashboard.create();
        sc.setParameters("dc", dcId);
        if (onlyCountAllocated) {
            sc.setParameters("state", State.Free);
        }
        sc.setJoinParameters("vlan", "vlanType", vlanType.toString());
        return customSearch(sc, null).get(0);
    }

    @Override
    public IPAddressVO findByAssociatedVmId(final long vmId) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("associatedWithVmId", vmId);

        return findOneBy(sc);
    }

    // for vm secondary ips case mapping is  IP1--> vmIp1, IP2-->vmIp2, etc
    // Used when vm is mapped to muliple to public ips
    @Override
    public List<IPAddressVO> findAllByAssociatedVmId(final long vmId) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("associatedWithVmId", vmId);

        return listBy(sc);
    }

    @Override
    public IPAddressVO findByIpAndSourceNetworkId(final long networkId, final String ipAddress) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("sourcenetwork", networkId);
        sc.setParameters("ipAddress", ipAddress);
        return findOneBy(sc);
    }

    @Override
    public IPAddressVO findByIpAndDcId(final long dcId, final String ipAddress) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("dataCenterId", dcId);
        sc.setParameters("ipAddress", ipAddress);
        return findOneBy(sc);
    }

    @Override
    public List<IPAddressVO> listByPhysicalNetworkId(final long physicalNetworkId) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);
        return listBy(sc);
    }

    @Override
    public List<IPAddressVO> listByAssociatedVpc(final long vpcId, final Boolean isSourceNat) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("vpcId", vpcId);

        if (isSourceNat != null) {
            sc.setParameters("sourceNat", isSourceNat);
        }

        return listBy(sc);
    }

    @Override
    public long countFreePublicIPs() {
        final SearchCriteria<Long> sc = CountFreePublicIps.create();
        sc.setParameters("state", State.Free);
        sc.setJoinParameters("vlans", "vlanType", VlanType.VirtualNetwork);
        return customSearch(sc, null).get(0);
    }

    @Override
    public long countFreeIPsInNetwork(final long networkId) {
        final SearchCriteria<Long> sc = CountFreePublicIps.create();
        sc.setParameters("state", State.Free);
        sc.setParameters("networkId", networkId);
        return customSearch(sc, null).get(0);
    }

    @Override
    public IPAddressVO findByVmIp(final String vmIp) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("associatedVmIp", vmIp);
        return findOneBy(sc);
    }

    @Override
    public IPAddressVO findByAssociatedVmIdAndVmIp(final long vmId, final String vmIp) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("associatedWithVmId", vmId);
        sc.setParameters("associatedVmIp", vmIp);
        return findOneBy(sc);
    }

    @Override
    public IPAddressVO findByIpAndNetworkId(final long networkId, final String ipAddress) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("ipAddress", ipAddress);
        return findOneBy(sc);
    }

    @Override
    public IPAddressVO findByIpAndVlanId(final String ipAddress, final long vlanid) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("ipAddress", ipAddress);
        sc.setParameters("vlan", vlanid);
        return findOneBy(sc);
    }

    @Override
    public long countFreeIpsInVlan(final long vlanDbId) {
        final SearchCriteria<IPAddressVO> sc = VlanDbIdSearchUnallocated.create();
        sc.setParameters("vlanDbId", vlanDbId);
        return listBy(sc).size();
    }

    @Override
    public void deletePublicIPRangeExceptAliasIP(final long vlanDbId, final String aliasIp) {
        final SearchCriteria<IPAddressVO> sc = DeleteAllExceptGivenIp.create();
        sc.setParameters("vlan", vlanDbId);
        sc.setParameters("ip", aliasIp);
        remove(sc);
    }

    @Override
    public boolean deletePublicIPRange(final long vlanDbId) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("vlan", vlanDbId);
        remove(sc);
        return true;
    }

    @Override
    public void lockRange(final long vlandbId) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("vlan", vlandbId);
        lockRows(sc, null, true);
    }

    @Override
    public List<IPAddressVO> listByAssociatedVmId(final long vmId) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("associatedWithVmId", vmId);
        return listBy(sc);
    }

    @Override
    public IPAddressVO findByVmIdAndNetworkId(final long networkId, final long vmId) {
        final SearchCriteria<IPAddressVO> sc = AllFieldsSearch.create();
        sc.setParameters("network", networkId);
        sc.setParameters("associatedWithVmId", vmId);
        return findOneBy(sc);
    }

    @Override
    @DB
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final IPAddressVO entry = findById(id);
        if (entry != null) {
            _tagsDao.removeByIdAndType(id, ResourceObjectType.SecurityGroup);
        }
        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }
}
