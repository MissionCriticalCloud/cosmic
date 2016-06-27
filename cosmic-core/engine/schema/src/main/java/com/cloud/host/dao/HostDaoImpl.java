package com.cloud.host.dao;

import com.cloud.agent.api.VgpuTypesInfo;
import com.cloud.cluster.agentlb.HostTransferMapVO;
import com.cloud.cluster.agentlb.dao.HostTransferMapDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.gpu.dao.HostGpuGroupsDao;
import com.cloud.gpu.dao.VGPUTypesDao;
import com.cloud.host.Host;
import com.cloud.host.Host.Type;
import com.cloud.host.HostTagVO;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.Status.Event;
import com.cloud.info.RunningHostCountInfo;
import com.cloud.org.Managed;
import com.cloud.resource.ResourceState;
import com.cloud.utils.DateUtil;
import com.cloud.utils.db.Attribute;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.JoinBuilder.JoinType;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.inject.Inject;
import javax.persistence.TableGenerator;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@Local(value = {HostDao.class})
@DB
@TableGenerator(name = "host_req_sq", table = "op_host", pkColumnName = "id", valueColumnName = "sequence", allocationSize = 1)
public class HostDaoImpl extends GenericDaoBase<HostVO, Long> implements HostDao { //FIXME: , ExternalIdDao {
    private static final Logger s_logger = LoggerFactory.getLogger(HostDaoImpl.class);
    private static final Logger status_logger = LoggerFactory.getLogger(Status.class);
    private static final Logger state_logger = LoggerFactory.getLogger(ResourceState.class);

    protected SearchBuilder<HostVO> TypePodDcStatusSearch;

    protected SearchBuilder<HostVO> IdStatusSearch;
    protected SearchBuilder<HostVO> TypeDcSearch;
    protected SearchBuilder<HostVO> TypeDcStatusSearch;
    protected SearchBuilder<HostVO> TypeClusterStatusSearch;
    protected SearchBuilder<HostVO> MsStatusSearch;
    protected SearchBuilder<HostVO> DcPrivateIpAddressSearch;
    protected SearchBuilder<HostVO> DcStorageIpAddressSearch;
    protected SearchBuilder<HostVO> PublicIpAddressSearch;

    protected SearchBuilder<HostVO> GuidSearch;
    protected SearchBuilder<HostVO> DcSearch;
    protected SearchBuilder<HostVO> PodSearch;
    protected SearchBuilder<HostVO> ClusterSearch;
    protected SearchBuilder<HostVO> TypeSearch;
    protected SearchBuilder<HostVO> StatusSearch;
    protected SearchBuilder<HostVO> ResourceStateSearch;
    protected SearchBuilder<HostVO> NameLikeSearch;
    protected SearchBuilder<HostVO> NameSearch;
    protected SearchBuilder<HostVO> SequenceSearch;
    protected SearchBuilder<HostVO> DirectlyConnectedSearch;
    protected SearchBuilder<HostVO> UnmanagedDirectConnectSearch;
    protected SearchBuilder<HostVO> UnmanagedApplianceSearch;
    protected SearchBuilder<HostVO> MaintenanceCountSearch;
    protected SearchBuilder<HostVO> ClusterStatusSearch;
    protected SearchBuilder<HostVO> TypeNameZoneSearch;
    protected SearchBuilder<HostVO> AvailHypevisorInZone;

    protected SearchBuilder<HostVO> DirectConnectSearch;
    protected SearchBuilder<HostVO> ManagedDirectConnectSearch;
    protected SearchBuilder<HostVO> ManagedRoutingServersSearch;
    protected SearchBuilder<HostVO> SecondaryStorageVMSearch;

    protected GenericSearchBuilder<HostVO, Long> HostIdSearch;
    protected GenericSearchBuilder<HostVO, Long> HostsInStatusSearch;
    protected GenericSearchBuilder<HostVO, Long> CountRoutingByDc;
    protected SearchBuilder<HostTransferMapVO> HostTransferSearch;
    protected SearchBuilder<ClusterVO> ClusterManagedSearch;
    protected SearchBuilder<HostVO> RoutingSearch;

    protected SearchBuilder<HostVO> HostsForReconnectSearch;
    protected GenericSearchBuilder<HostVO, Long> ClustersOwnedByMSSearch;
    protected GenericSearchBuilder<HostVO, Long> ClustersForHostsNotOwnedByAnyMSSearch;
    protected GenericSearchBuilder<ClusterVO, Long> AllClustersSearch;
    protected SearchBuilder<HostVO> HostsInClusterSearch;

    protected Attribute _statusAttr;
    protected Attribute _resourceStateAttr;
    protected Attribute _msIdAttr;
    protected Attribute _pingTimeAttr;

    @Inject
    protected HostDetailsDao _detailsDao;
    @Inject
    protected HostGpuGroupsDao _hostGpuGroupsDao;
    @Inject
    protected VGPUTypesDao _vgpuTypesDao;
    @Inject
    protected HostTagsDao _hostTagsDao;
    @Inject
    protected HostTransferMapDao _hostTransferDao;
    @Inject
    protected ClusterDao _clusterDao;

    public HostDaoImpl() {
        super();
    }

    @PostConstruct
    public void init() {

        MaintenanceCountSearch = createSearchBuilder();
        MaintenanceCountSearch.and("cluster", MaintenanceCountSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        MaintenanceCountSearch.and("resourceState", MaintenanceCountSearch.entity().getResourceState(), SearchCriteria.Op.IN);
        MaintenanceCountSearch.done();

        TypePodDcStatusSearch = createSearchBuilder();
        final HostVO entity = TypePodDcStatusSearch.entity();
        TypePodDcStatusSearch.and("type", entity.getType(), SearchCriteria.Op.EQ);
        TypePodDcStatusSearch.and("pod", entity.getPodId(), SearchCriteria.Op.EQ);
        TypePodDcStatusSearch.and("dc", entity.getDataCenterId(), SearchCriteria.Op.EQ);
        TypePodDcStatusSearch.and("cluster", entity.getClusterId(), SearchCriteria.Op.EQ);
        TypePodDcStatusSearch.and("status", entity.getStatus(), SearchCriteria.Op.EQ);
        TypePodDcStatusSearch.and("resourceState", entity.getResourceState(), SearchCriteria.Op.EQ);
        TypePodDcStatusSearch.done();

        MsStatusSearch = createSearchBuilder();
        MsStatusSearch.and("ms", MsStatusSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
        MsStatusSearch.and("type", MsStatusSearch.entity().getType(), SearchCriteria.Op.EQ);
        MsStatusSearch.and("resourceState", MsStatusSearch.entity().getResourceState(), SearchCriteria.Op.NIN);
        MsStatusSearch.done();

        TypeDcSearch = createSearchBuilder();
        TypeDcSearch.and("type", TypeDcSearch.entity().getType(), SearchCriteria.Op.EQ);
        TypeDcSearch.and("dc", TypeDcSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        TypeDcSearch.done();

        SecondaryStorageVMSearch = createSearchBuilder();
        SecondaryStorageVMSearch.and("type", SecondaryStorageVMSearch.entity().getType(), SearchCriteria.Op.EQ);
        SecondaryStorageVMSearch.and("dc", SecondaryStorageVMSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        SecondaryStorageVMSearch.and("status", SecondaryStorageVMSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        SecondaryStorageVMSearch.done();

        TypeDcStatusSearch = createSearchBuilder();
        TypeDcStatusSearch.and("type", TypeDcStatusSearch.entity().getType(), SearchCriteria.Op.EQ);
        TypeDcStatusSearch.and("dc", TypeDcStatusSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        TypeDcStatusSearch.and("status", TypeDcStatusSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        TypeDcStatusSearch.and("resourceState", TypeDcStatusSearch.entity().getResourceState(), SearchCriteria.Op.EQ);
        TypeDcStatusSearch.done();

        TypeClusterStatusSearch = createSearchBuilder();
        TypeClusterStatusSearch.and("type", TypeClusterStatusSearch.entity().getType(), SearchCriteria.Op.EQ);
        TypeClusterStatusSearch.and("cluster", TypeClusterStatusSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        TypeClusterStatusSearch.and("status", TypeClusterStatusSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        TypeClusterStatusSearch.and("resourceState", TypeClusterStatusSearch.entity().getResourceState(), SearchCriteria.Op.EQ);
        TypeClusterStatusSearch.done();

        IdStatusSearch = createSearchBuilder();
        IdStatusSearch.and("id", IdStatusSearch.entity().getId(), SearchCriteria.Op.EQ);
        IdStatusSearch.and("states", IdStatusSearch.entity().getStatus(), SearchCriteria.Op.IN);
        IdStatusSearch.done();

        DcPrivateIpAddressSearch = createSearchBuilder();
        DcPrivateIpAddressSearch.and("privateIpAddress", DcPrivateIpAddressSearch.entity().getPrivateIpAddress(), SearchCriteria.Op.EQ);
        DcPrivateIpAddressSearch.and("dc", DcPrivateIpAddressSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        DcPrivateIpAddressSearch.done();

        DcStorageIpAddressSearch = createSearchBuilder();
        DcStorageIpAddressSearch.and("storageIpAddress", DcStorageIpAddressSearch.entity().getStorageIpAddress(), SearchCriteria.Op.EQ);
        DcStorageIpAddressSearch.and("dc", DcStorageIpAddressSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        DcStorageIpAddressSearch.done();

        PublicIpAddressSearch = createSearchBuilder();
        PublicIpAddressSearch.and("publicIpAddress", PublicIpAddressSearch.entity().getPublicIpAddress(), SearchCriteria.Op.EQ);
        PublicIpAddressSearch.done();

        GuidSearch = createSearchBuilder();
        GuidSearch.and("guid", GuidSearch.entity().getGuid(), SearchCriteria.Op.EQ);
        GuidSearch.done();

        DcSearch = createSearchBuilder();
        DcSearch.and("dc", DcSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        DcSearch.and("type", DcSearch.entity().getType(), Op.EQ);
        DcSearch.and("status", DcSearch.entity().getStatus(), Op.EQ);
        DcSearch.and("resourceState", DcSearch.entity().getResourceState(), Op.EQ);
        DcSearch.done();

        ClusterStatusSearch = createSearchBuilder();
        ClusterStatusSearch.and("cluster", ClusterStatusSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        ClusterStatusSearch.and("status", ClusterStatusSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        ClusterStatusSearch.done();

        TypeNameZoneSearch = createSearchBuilder();
        TypeNameZoneSearch.and("name", TypeNameZoneSearch.entity().getName(), SearchCriteria.Op.EQ);
        TypeNameZoneSearch.and("type", TypeNameZoneSearch.entity().getType(), SearchCriteria.Op.EQ);
        TypeNameZoneSearch.and("zoneId", TypeNameZoneSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        TypeNameZoneSearch.done();

        PodSearch = createSearchBuilder();
        PodSearch.and("podId", PodSearch.entity().getPodId(), SearchCriteria.Op.EQ);
        PodSearch.done();

        ClusterSearch = createSearchBuilder();
        ClusterSearch.and("clusterId", ClusterSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        ClusterSearch.done();

        TypeSearch = createSearchBuilder();
        TypeSearch.and("type", TypeSearch.entity().getType(), SearchCriteria.Op.EQ);
        TypeSearch.done();

        StatusSearch = createSearchBuilder();
        StatusSearch.and("status", StatusSearch.entity().getStatus(), SearchCriteria.Op.IN);
        StatusSearch.done();

        ResourceStateSearch = createSearchBuilder();
        ResourceStateSearch.and("resourceState", ResourceStateSearch.entity().getResourceState(), SearchCriteria.Op.IN);
        ResourceStateSearch.done();

        NameLikeSearch = createSearchBuilder();
        NameLikeSearch.and("name", NameLikeSearch.entity().getName(), SearchCriteria.Op.LIKE);
        NameLikeSearch.done();

        NameSearch = createSearchBuilder();
        NameSearch.and("name", NameSearch.entity().getName(), SearchCriteria.Op.EQ);
        NameSearch.done();

        SequenceSearch = createSearchBuilder();
        SequenceSearch.and("id", SequenceSearch.entity().getId(), SearchCriteria.Op.EQ);
        // SequenceSearch.addRetrieve("sequence", SequenceSearch.entity().getSequence());
        SequenceSearch.done();

        DirectlyConnectedSearch = createSearchBuilder();
        DirectlyConnectedSearch.and("resource", DirectlyConnectedSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        DirectlyConnectedSearch.and("ms", DirectlyConnectedSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
        DirectlyConnectedSearch.and("statuses", DirectlyConnectedSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        DirectlyConnectedSearch.and("resourceState", DirectlyConnectedSearch.entity().getResourceState(), SearchCriteria.Op.NOTIN);
        DirectlyConnectedSearch.done();

        UnmanagedDirectConnectSearch = createSearchBuilder();
        UnmanagedDirectConnectSearch.and("resource", UnmanagedDirectConnectSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        UnmanagedDirectConnectSearch.and("server", UnmanagedDirectConnectSearch.entity().getManagementServerId(), SearchCriteria.Op.NULL);
        UnmanagedDirectConnectSearch.and("lastPinged", UnmanagedDirectConnectSearch.entity().getLastPinged(), SearchCriteria.Op.LTEQ);
        UnmanagedDirectConnectSearch.and("resourceStates", UnmanagedDirectConnectSearch.entity().getResourceState(), SearchCriteria.Op.NIN);
        UnmanagedDirectConnectSearch.and("clusterIn", UnmanagedDirectConnectSearch.entity().getClusterId(), SearchCriteria.Op.IN);
        /*
         * UnmanagedDirectConnectSearch.op(SearchCriteria.Op.OR, "managementServerId",
         * UnmanagedDirectConnectSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
         * UnmanagedDirectConnectSearch.and("lastPinged", UnmanagedDirectConnectSearch.entity().getLastPinged(),
         * SearchCriteria.Op.LTEQ); UnmanagedDirectConnectSearch.cp(); UnmanagedDirectConnectSearch.cp();
         */
        try {
            HostTransferSearch = _hostTransferDao.createSearchBuilder();
        } catch (final Throwable e) {
            s_logger.debug("error", e);
        }
        HostTransferSearch.and("id", HostTransferSearch.entity().getId(), SearchCriteria.Op.NULL);
        UnmanagedDirectConnectSearch.join("hostTransferSearch", HostTransferSearch, HostTransferSearch.entity().getId(), UnmanagedDirectConnectSearch.entity().getId(),
                JoinType.LEFTOUTER);
        ClusterManagedSearch = _clusterDao.createSearchBuilder();
        ClusterManagedSearch.and("managed", ClusterManagedSearch.entity().getManagedState(), SearchCriteria.Op.EQ);
        UnmanagedDirectConnectSearch.join("ClusterManagedSearch", ClusterManagedSearch, ClusterManagedSearch.entity().getId(), UnmanagedDirectConnectSearch.entity()
                                                                                                                                                           .getClusterId(),
                JoinType.INNER);
        UnmanagedDirectConnectSearch.done();

        DirectConnectSearch = createSearchBuilder();
        DirectConnectSearch.and("resource", DirectConnectSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        DirectConnectSearch.and("id", DirectConnectSearch.entity().getId(), SearchCriteria.Op.EQ);
        DirectConnectSearch.and().op("nullserver", DirectConnectSearch.entity().getManagementServerId(), SearchCriteria.Op.NULL);
        DirectConnectSearch.or("server", DirectConnectSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
        DirectConnectSearch.cp();
        DirectConnectSearch.done();

        UnmanagedApplianceSearch = createSearchBuilder();
        UnmanagedApplianceSearch.and("resource", UnmanagedApplianceSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        UnmanagedApplianceSearch.and("server", UnmanagedApplianceSearch.entity().getManagementServerId(), SearchCriteria.Op.NULL);
        UnmanagedApplianceSearch.and("types", UnmanagedApplianceSearch.entity().getType(), SearchCriteria.Op.IN);
        UnmanagedApplianceSearch.and("lastPinged", UnmanagedApplianceSearch.entity().getLastPinged(), SearchCriteria.Op.LTEQ);
        UnmanagedApplianceSearch.done();

        AvailHypevisorInZone = createSearchBuilder();
        AvailHypevisorInZone.and("zoneId", AvailHypevisorInZone.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        AvailHypevisorInZone.and("hostId", AvailHypevisorInZone.entity().getId(), SearchCriteria.Op.NEQ);
        AvailHypevisorInZone.and("type", AvailHypevisorInZone.entity().getType(), SearchCriteria.Op.EQ);
        AvailHypevisorInZone.groupBy(AvailHypevisorInZone.entity().getHypervisorType());
        AvailHypevisorInZone.done();

        HostsInStatusSearch = createSearchBuilder(Long.class);
        HostsInStatusSearch.selectFields(HostsInStatusSearch.entity().getId());
        HostsInStatusSearch.and("dc", HostsInStatusSearch.entity().getDataCenterId(), Op.EQ);
        HostsInStatusSearch.and("pod", HostsInStatusSearch.entity().getPodId(), Op.EQ);
        HostsInStatusSearch.and("cluster", HostsInStatusSearch.entity().getClusterId(), Op.EQ);
        HostsInStatusSearch.and("type", HostsInStatusSearch.entity().getType(), Op.EQ);
        HostsInStatusSearch.and("statuses", HostsInStatusSearch.entity().getStatus(), Op.IN);
        HostsInStatusSearch.done();

        CountRoutingByDc = createSearchBuilder(Long.class);
        CountRoutingByDc.select(null, Func.COUNT, null);
        CountRoutingByDc.and("dc", CountRoutingByDc.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        CountRoutingByDc.and("type", CountRoutingByDc.entity().getType(), SearchCriteria.Op.EQ);
        CountRoutingByDc.and("status", CountRoutingByDc.entity().getStatus(), SearchCriteria.Op.EQ);
        CountRoutingByDc.done();

        ManagedDirectConnectSearch = createSearchBuilder();
        ManagedDirectConnectSearch.and("resource", ManagedDirectConnectSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        ManagedDirectConnectSearch.and("server", ManagedDirectConnectSearch.entity().getManagementServerId(), SearchCriteria.Op.NULL);
        ManagedDirectConnectSearch.done();

        ManagedRoutingServersSearch = createSearchBuilder();
        ManagedRoutingServersSearch.and("server", ManagedRoutingServersSearch.entity().getManagementServerId(), SearchCriteria.Op.NNULL);
        ManagedRoutingServersSearch.and("type", ManagedRoutingServersSearch.entity().getType(), SearchCriteria.Op.EQ);
        ManagedRoutingServersSearch.done();

        RoutingSearch = createSearchBuilder();
        RoutingSearch.and("type", RoutingSearch.entity().getType(), SearchCriteria.Op.EQ);
        RoutingSearch.done();

        HostsForReconnectSearch = createSearchBuilder();
        HostsForReconnectSearch.and("resource", HostsForReconnectSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        HostsForReconnectSearch.and("server", HostsForReconnectSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
        HostsForReconnectSearch.and("lastPinged", HostsForReconnectSearch.entity().getLastPinged(), SearchCriteria.Op.LTEQ);
        HostsForReconnectSearch.and("resourceStates", HostsForReconnectSearch.entity().getResourceState(), SearchCriteria.Op.NIN);
        HostsForReconnectSearch.and("cluster", HostsForReconnectSearch.entity().getClusterId(), SearchCriteria.Op.NNULL);
        HostsForReconnectSearch.and("status", HostsForReconnectSearch.entity().getStatus(), SearchCriteria.Op.IN);
        HostsForReconnectSearch.done();

        ClustersOwnedByMSSearch = createSearchBuilder(Long.class);
        ClustersOwnedByMSSearch.select(null, Func.DISTINCT, ClustersOwnedByMSSearch.entity().getClusterId());
        ClustersOwnedByMSSearch.and("resource", ClustersOwnedByMSSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        ClustersOwnedByMSSearch.and("cluster", ClustersOwnedByMSSearch.entity().getClusterId(), SearchCriteria.Op.NNULL);
        ClustersOwnedByMSSearch.and("server", ClustersOwnedByMSSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
        ClustersOwnedByMSSearch.done();

        ClustersForHostsNotOwnedByAnyMSSearch = createSearchBuilder(Long.class);
        ClustersForHostsNotOwnedByAnyMSSearch.select(null, Func.DISTINCT, ClustersForHostsNotOwnedByAnyMSSearch.entity().getClusterId());
        ClustersForHostsNotOwnedByAnyMSSearch.and("resource", ClustersForHostsNotOwnedByAnyMSSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        ClustersForHostsNotOwnedByAnyMSSearch.and("cluster", ClustersForHostsNotOwnedByAnyMSSearch.entity().getClusterId(), SearchCriteria.Op.NNULL);
        ClustersForHostsNotOwnedByAnyMSSearch.and("server", ClustersForHostsNotOwnedByAnyMSSearch.entity().getManagementServerId(), SearchCriteria.Op.NULL);
        ClustersForHostsNotOwnedByAnyMSSearch.done();

        AllClustersSearch = _clusterDao.createSearchBuilder(Long.class);
        AllClustersSearch.select(null, Func.NATIVE, AllClustersSearch.entity().getId());
        AllClustersSearch.and("managed", AllClustersSearch.entity().getManagedState(), SearchCriteria.Op.EQ);
        AllClustersSearch.done();

        HostsInClusterSearch = createSearchBuilder();
        HostsInClusterSearch.and("resource", HostsInClusterSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        HostsInClusterSearch.and("cluster", HostsInClusterSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        HostsInClusterSearch.and("server", HostsInClusterSearch.entity().getManagementServerId(), SearchCriteria.Op.NNULL);
        HostsInClusterSearch.done();

        HostIdSearch = createSearchBuilder(Long.class);
        HostIdSearch.selectFields(HostIdSearch.entity().getId());
        HostIdSearch.and("dataCenterId", HostIdSearch.entity().getDataCenterId(), Op.EQ);
        HostIdSearch.done();

        _statusAttr = _allAttributes.get("status");
        _msIdAttr = _allAttributes.get("managementServerId");
        _pingTimeAttr = _allAttributes.get("lastPinged");
        _resourceStateAttr = _allAttributes.get("resourceState");

        assert (_statusAttr != null && _msIdAttr != null && _pingTimeAttr != null) : "Couldn't find one of these attributes";
    }

    @Override
    public long countBy(final long clusterId, final ResourceState... states) {
        final SearchCriteria<HostVO> sc = MaintenanceCountSearch.create();

        sc.setParameters("resourceState", (Object[]) states);
        sc.setParameters("cluster", clusterId);

        final List<HostVO> hosts = listBy(sc);
        return hosts.size();
    }

    @Override
    public void markHostsAsDisconnected(final long msId, final long lastPing) {
        SearchCriteria<HostVO> sc = MsStatusSearch.create();
        sc.setParameters("ms", msId);

        HostVO host = createForUpdate();
        host.setLastPinged(lastPing);
        host.setDisconnectedOn(new Date());
        UpdateBuilder ub = getUpdateBuilder(host);
        ub.set(host, "status", Status.Disconnected);

        update(ub, sc, null);

        sc = MsStatusSearch.create();
        sc.setParameters("ms", msId);

        host = createForUpdate();
        host.setManagementServerId(null);
        host.setLastPinged(lastPing);
        host.setDisconnectedOn(new Date());
        ub = getUpdateBuilder(host);
        update(ub, sc, null);
    }

    @DB
    @Override
    public List<HostVO> findLostHosts(final long timeout) {
        final List<HostVO> result = new ArrayList<>();
        final String sql =
                "select h.id from host h left join  cluster c on h.cluster_id=c.id where h.mgmt_server_id is not null and h.last_ping < ? and h.status in ('Up', 'Updating', " +
                        "'Disconnected', 'Connecting') and h.type not in ('ExternalFirewall', 'ExternalLoadBalancer', 'TrafficMonitor', 'SecondaryStorage', " +
                        "'LocalSecondaryStorage', 'L2Networking') and (h.cluster_id is null or c.managed_state = 'Managed') ;";
        try (
                TransactionLegacy txn = TransactionLegacy.currentTxn();
                PreparedStatement pstmt = txn.prepareStatement(sql)) {
            pstmt.setLong(1, timeout);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    final long id = rs.getLong(1); //ID column
                    result.add(findById(id));
                }
            }
        } catch (final SQLException e) {
            s_logger.warn("Exception: ", e);
        }
        return result;
    }

    @Override
    @DB
    public List<HostVO> findAndUpdateDirectAgentToLoad(final long lastPingSecondsAfter, final Long limit, final long managementServerId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();

        txn.start();
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Resetting hosts suitable for reconnect");
        }
        // reset hosts that are suitable candidates for reconnect
        resetHosts(managementServerId, lastPingSecondsAfter);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Completed resetting hosts suitable for reconnect");
        }

        final List<HostVO> assignedHosts = new ArrayList<>();

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Acquiring hosts for clusters already owned by this management server");
        }
        List<Long> clusters = findClustersOwnedByManagementServer(managementServerId);
        if (clusters.size() > 0) {
            // handle clusters already owned by @managementServerId
            final SearchCriteria<HostVO> sc = UnmanagedDirectConnectSearch.create();
            sc.setParameters("lastPinged", lastPingSecondsAfter);
            sc.setJoinParameters("ClusterManagedSearch", "managed", Managed.ManagedState.Managed);
            sc.setParameters("clusterIn", clusters.toArray());
            final List<HostVO> unmanagedHosts = lockRows(sc, new Filter(HostVO.class, "clusterId", true, 0L, limit), true); // host belongs to clusters owned by @managementServerId
            final StringBuilder sb = new StringBuilder();
            for (final HostVO host : unmanagedHosts) {
                host.setManagementServerId(managementServerId);
                update(host.getId(), host);
                assignedHosts.add(host);
                sb.append(host.getId());
                sb.append(" ");
            }
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Following hosts got acquired for clusters already owned: " + sb.toString());
            }
        }
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Completed acquiring hosts for clusters already owned by this management server");
        }

        if (assignedHosts.size() < limit) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Acquiring hosts for clusters not owned by any management server");
            }
            // for remaining hosts not owned by any MS check if they can be owned (by owning full cluster)
            clusters = findClustersForHostsNotOwnedByAnyManagementServer();
            List<Long> updatedClusters = clusters;
            if (clusters.size() > limit) {
                updatedClusters = clusters.subList(0, limit.intValue());
            }
            if (updatedClusters.size() > 0) {
                final SearchCriteria<HostVO> sc = UnmanagedDirectConnectSearch.create();
                sc.setParameters("lastPinged", lastPingSecondsAfter);
                sc.setJoinParameters("ClusterManagedSearch", "managed", Managed.ManagedState.Managed);
                sc.setParameters("clusterIn", updatedClusters.toArray());
                final List<HostVO> unmanagedHosts = lockRows(sc, null, true);

                // group hosts based on cluster
                final Map<Long, List<HostVO>> hostMap = new HashMap<>();
                for (final HostVO host : unmanagedHosts) {
                    if (hostMap.get(host.getClusterId()) == null) {
                        hostMap.put(host.getClusterId(), new ArrayList<>());
                    }
                    hostMap.get(host.getClusterId()).add(host);
                }

                final StringBuilder sb = new StringBuilder();
                for (final Long clusterId : hostMap.keySet()) {
                    if (canOwnCluster(clusterId)) { // cluster is not owned by any other MS, so @managementServerId can own it
                        final List<HostVO> hostList = hostMap.get(clusterId);
                        for (final HostVO host : hostList) {
                            host.setManagementServerId(managementServerId);
                            update(host.getId(), host);
                            assignedHosts.add(host);
                            sb.append(host.getId());
                            sb.append(" ");
                        }
                    }
                    if (assignedHosts.size() > limit) {
                        break;
                    }
                }
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("Following hosts got acquired from newly owned clusters: " + sb.toString());
                }
            }
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Completed acquiring hosts for clusters not owned by any management server");
            }
        }
        txn.commit();

        return assignedHosts;
    }

    @Override
    @DB
    public List<RunningHostCountInfo> getRunningHostCounts(final Date cutTime) {
        final String sql =
                "select * from (" + "select h.data_center_id, h.type, count(*) as count from host as h INNER JOIN mshost as m ON h.mgmt_server_id=m.msid "
                        + "where h.status='Up' and h.type='SecondaryStorage' and m.last_update > ? " + "group by h.data_center_id, h.type " + "UNION ALL "
                        + "select h.data_center_id, h.type, count(*) as count from host as h INNER JOIN mshost as m ON h.mgmt_server_id=m.msid "
                        + "where h.status='Up' and h.type='Routing' and m.last_update > ? " + "group by h.data_center_id, h.type) as t " + "ORDER by t.data_center_id, t.type";

        final ArrayList<RunningHostCountInfo> l = new ArrayList<>();

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        try {
            pstmt = txn.prepareAutoCloseStatement(sql);
            final String gmtCutTime = DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), cutTime);
            pstmt.setString(1, gmtCutTime);
            pstmt.setString(2, gmtCutTime);

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                final RunningHostCountInfo info = new RunningHostCountInfo();
                info.setDcId(rs.getLong(1));
                info.setHostType(rs.getString(2));
                info.setCount(rs.getInt(3));

                l.add(info);
            }
        } catch (final SQLException e) {
            s_logger.debug("SQLException caught", e);
        }
        return l;
    }

    @Override
    public long getNextSequence(final long hostId) {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("getNextSequence(), hostId: " + hostId);
        }

        final TableGenerator tg = _tgs.get("host_req_sq");
        assert tg != null : "how can this be wrong!";

        return s_seqFetcher.getNextSequence(Long.class, tg, hostId);
    }

    @Override
    public void loadDetails(final HostVO host) {
        final Map<String, String> details = _detailsDao.findDetails(host.getId());
        host.setDetails(details);
    }

    @Override
    public void saveDetails(final HostVO host) {
        final Map<String, String> details = host.getDetails();
        if (details == null) {
            return;
        }
        _detailsDao.persist(host.getId(), details);
    }

    @Override
    public void loadHostTags(final HostVO host) {
        final List<String> hostTags = _hostTagsDao.gethostTags(host.getId());
        host.setHostTags(hostTags);
    }

    @Override
    public List<HostVO> listByHostTag(final Host.Type type, final Long clusterId, final Long podId, final long dcId, final String hostTag) {

        final SearchBuilder<HostTagVO> hostTagSearch = _hostTagsDao.createSearchBuilder();
        final HostTagVO tagEntity = hostTagSearch.entity();
        hostTagSearch.and("tag", tagEntity.getTag(), SearchCriteria.Op.EQ);

        final SearchBuilder<HostVO> hostSearch = createSearchBuilder();
        final HostVO entity = hostSearch.entity();
        hostSearch.and("type", entity.getType(), SearchCriteria.Op.EQ);
        hostSearch.and("pod", entity.getPodId(), SearchCriteria.Op.EQ);
        hostSearch.and("dc", entity.getDataCenterId(), SearchCriteria.Op.EQ);
        hostSearch.and("cluster", entity.getClusterId(), SearchCriteria.Op.EQ);
        hostSearch.and("status", entity.getStatus(), SearchCriteria.Op.EQ);
        hostSearch.and("resourceState", entity.getResourceState(), SearchCriteria.Op.EQ);
        hostSearch.join("hostTagSearch", hostTagSearch, entity.getId(), tagEntity.getHostId(), JoinBuilder.JoinType.INNER);

        final SearchCriteria<HostVO> sc = hostSearch.create();
        sc.setJoinParameters("hostTagSearch", "tag", hostTag);
        sc.setParameters("type", type.toString());
        if (podId != null) {
            sc.setParameters("pod", podId);
        }
        if (clusterId != null) {
            sc.setParameters("cluster", clusterId);
        }
        sc.setParameters("dc", dcId);
        sc.setParameters("status", Status.Up.toString());
        sc.setParameters("resourceState", ResourceState.Enabled.toString());

        return listBy(sc);
    }

    /*TODO: this is used by mycloud, check if it needs resource state Enabled */
    @Override
    public long countRoutingHostsByDataCenter(final long dcId) {
        final SearchCriteria<Long> sc = CountRoutingByDc.create();
        sc.setParameters("dc", dcId);
        sc.setParameters("type", Host.Type.Routing);
        sc.setParameters("status", Status.Up.toString());
        return customSearch(sc, null).get(0);
    }

    @Override
    @DB
    public List<HostVO> findAndUpdateApplianceToLoad(final long lastPingSecondsAfter, final long managementServerId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();

        txn.start();
        final SearchCriteria<HostVO> sc = UnmanagedApplianceSearch.create();
        sc.setParameters("lastPinged", lastPingSecondsAfter);
        sc.setParameters("types", Type.ExternalDhcp, Type.ExternalFirewall, Type.ExternalLoadBalancer, Type.TrafficMonitor,
                Type.L2Networking);
        final List<HostVO> hosts = lockRows(sc, null, true);

        for (final HostVO host : hosts) {
            host.setManagementServerId(managementServerId);
            update(host.getId(), host);
        }

        txn.commit();

        return hosts;
    }

    @Override
    @DB
    public boolean update(final Long hostId, final HostVO host) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        final boolean persisted = super.update(hostId, host);
        if (!persisted) {
            return persisted;
        }

        saveDetails(host);
        saveHostTags(host);
        saveGpuRecords(host);

        txn.commit();

        return persisted;
    }

    protected void saveHostTags(final HostVO host) {
        final List<String> hostTags = host.getHostTags();
        if (hostTags == null || (hostTags != null && hostTags.isEmpty())) {
            return;
        }
        _hostTagsDao.persist(host.getId(), hostTags);
    }

    protected void saveGpuRecords(final HostVO host) {
        final HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails = host.getGpuGroupDetails();
        if (groupDetails != null) {
            // Create/Update GPU group entries
            _hostGpuGroupsDao.persist(host.getId(), new ArrayList<>(groupDetails.keySet()));
            // Create/Update VGPU types entries
            _vgpuTypesDao.persist(host.getId(), groupDetails);
        }
    }

    @Override
    @DB
    public HostVO persist(final HostVO host) {
        final String InsertSequenceSql = "INSERT INTO op_host(id) VALUES(?)";

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        final HostVO dbHost = super.persist(host);

        try {
            final PreparedStatement pstmt = txn.prepareAutoCloseStatement(InsertSequenceSql);
            pstmt.setLong(1, dbHost.getId());
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new CloudRuntimeException("Unable to persist the sequence number for this host");
        }

        saveDetails(host);
        loadDetails(dbHost);
        saveHostTags(host);
        loadHostTags(dbHost);
        saveGpuRecords(host);

        txn.commit();

        return dbHost;
    }

    @Override
    public boolean updateResourceState(final ResourceState oldState, final ResourceState.Event event, final ResourceState newState, final Host vo) {
        final HostVO host = (HostVO) vo;
        final SearchBuilder<HostVO> sb = createSearchBuilder();
        sb.and("resource_state", sb.entity().getResourceState(), SearchCriteria.Op.EQ);
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.done();

        final SearchCriteria<HostVO> sc = sb.create();

        sc.setParameters("resource_state", oldState);
        sc.setParameters("id", host.getId());

        final UpdateBuilder ub = getUpdateBuilder(host);
        ub.set(host, _resourceStateAttr, newState);
        final int result = update(ub, sc, null);
        assert result <= 1 : "How can this update " + result + " rows? ";

        if (state_logger.isDebugEnabled() && result == 0) {
            final HostVO ho = findById(host.getId());
            assert ho != null : "How how how? : " + host.getId();

            final StringBuilder str = new StringBuilder("Unable to update resource state: [");
            str.append("m = " + host.getId());
            str.append("; name = " + host.getName());
            str.append("; old state = " + oldState);
            str.append("; event = " + event);
            str.append("; new state = " + newState + "]");
            state_logger.debug(str.toString());
        } else {
            final StringBuilder msg = new StringBuilder("Resource state update: [");
            msg.append("id = " + host.getId());
            msg.append("; name = " + host.getName());
            msg.append("; old state = " + oldState);
            msg.append("; event = " + event);
            msg.append("; new state = " + newState + "]");
            state_logger.debug(msg.toString());
        }

        return result > 0;
    }

    @Override
    public HostVO findByGuid(final String guid) {
        final SearchCriteria<HostVO> sc = GuidSearch.create("guid", guid);
        return findOneBy(sc);
    }

    @Override
    public HostVO findByTypeNameAndZoneId(final long zoneId, final String name, final Host.Type type) {
        final SearchCriteria<HostVO> sc = TypeNameZoneSearch.create();
        sc.setParameters("type", type);
        sc.setParameters("name", name);
        sc.setParameters("zoneId", zoneId);
        return findOneBy(sc);
    }

    @Override
    public List<HostVO> findHypervisorHostInCluster(final long clusterId) {
        final SearchCriteria<HostVO> sc = TypeClusterStatusSearch.create();
        sc.setParameters("type", Host.Type.Routing);
        sc.setParameters("cluster", clusterId);
        sc.setParameters("status", Status.Up);
        sc.setParameters("resourceState", ResourceState.Enabled);

        return listBy(sc);
    }

    @Override
    public List<HostVO> listAllUpAndEnabledNonHAHosts(final Type type, final Long clusterId, final Long podId, final long dcId, final String haTag) {
        SearchBuilder<HostTagVO> hostTagSearch = null;
        if (haTag != null && !haTag.isEmpty()) {
            hostTagSearch = _hostTagsDao.createSearchBuilder();
            hostTagSearch.and().op("tag", hostTagSearch.entity().getTag(), SearchCriteria.Op.NEQ);
            hostTagSearch.or("tagNull", hostTagSearch.entity().getTag(), SearchCriteria.Op.NULL);
            hostTagSearch.cp();
        }

        final SearchBuilder<HostVO> hostSearch = createSearchBuilder();

        hostSearch.and("type", hostSearch.entity().getType(), SearchCriteria.Op.EQ);
        hostSearch.and("clusterId", hostSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        hostSearch.and("podId", hostSearch.entity().getPodId(), SearchCriteria.Op.EQ);
        hostSearch.and("zoneId", hostSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        hostSearch.and("status", hostSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        hostSearch.and("resourceState", hostSearch.entity().getResourceState(), SearchCriteria.Op.EQ);

        if (haTag != null && !haTag.isEmpty()) {
            hostSearch.join("hostTagSearch", hostTagSearch, hostSearch.entity().getId(), hostTagSearch.entity().getHostId(), JoinBuilder.JoinType.LEFTOUTER);
        }

        final SearchCriteria<HostVO> sc = hostSearch.create();

        if (haTag != null && !haTag.isEmpty()) {
            sc.setJoinParameters("hostTagSearch", "tag", haTag);
        }

        if (type != null) {
            sc.setParameters("type", type);
        }

        if (clusterId != null) {
            sc.setParameters("clusterId", clusterId);
        }

        if (podId != null) {
            sc.setParameters("podId", podId);
        }

        sc.setParameters("zoneId", dcId);
        sc.setParameters("status", Status.Up);
        sc.setParameters("resourceState", ResourceState.Enabled);

        return listBy(sc);
    }

    @Override
    public List<HostVO> findByPodId(final Long podId) {
        final SearchCriteria<HostVO> sc = PodSearch.create();
        sc.setParameters("podId", podId);
        return listBy(sc);
    }

    @Override
    public List<HostVO> findByClusterId(final Long clusterId) {
        final SearchCriteria<HostVO> sc = ClusterSearch.create();
        sc.setParameters("clusterId", clusterId);
        return listBy(sc);
    }

    @Override
    public List<HostVO> listByDataCenterId(final long id) {
        final SearchCriteria<HostVO> sc = DcSearch.create();
        sc.setParameters("dc", id);
        sc.setParameters("status", Status.Up);
        sc.setParameters("type", Host.Type.Routing);
        sc.setParameters("resourceState", ResourceState.Enabled);

        return listBy(sc);
    }

    @Override
    public List<Long> listAllHosts(final long zoneId) {
        final SearchCriteria<Long> sc = HostIdSearch.create();
        sc.addAnd("dataCenterId", SearchCriteria.Op.EQ, zoneId);
        return customSearch(sc, null);
    }

    @Override
    public HostVO findByPublicIp(final String publicIp) {
        final SearchCriteria<HostVO> sc = PublicIpAddressSearch.create();
        sc.setParameters("publicIpAddress", publicIp);
        return findOneBy(sc);
    }

    @Override
    public List<HostVO> listByType(final Host.Type type) {
        final SearchCriteria<HostVO> sc = TypeSearch.create();
        sc.setParameters("type", type);
        return listBy(sc);
    }

    /*
     * Find hosts which is in Disconnected, Down, Alert and ping timeout and server is not null, set server to null
     */
    private void resetHosts(final long managementServerId, final long lastPingSecondsAfter) {
        final SearchCriteria<HostVO> sc = HostsForReconnectSearch.create();
        sc.setParameters("server", managementServerId);
        sc.setParameters("lastPinged", lastPingSecondsAfter);
        sc.setParameters("status", Status.Disconnected, Status.Down, Status.Alert);

        final StringBuilder sb = new StringBuilder();
        final List<HostVO> hosts = lockRows(sc, null, true); // exclusive lock
        for (final HostVO host : hosts) {
            host.setManagementServerId(null);
            update(host.getId(), host);
            sb.append(host.getId());
            sb.append(" ");
        }

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Following hosts got reset: " + sb.toString());
        }
    }

    /*
     * Returns a list of cluster owned by @managementServerId
     */
    private List<Long> findClustersOwnedByManagementServer(final long managementServerId) {
        final SearchCriteria<Long> sc = ClustersOwnedByMSSearch.create();
        sc.setParameters("server", managementServerId);

        final List<Long> clusters = customSearch(sc, null);
        return clusters;
    }

    /*
     * Returns clusters based on the list of hosts not owned by any MS
     */
    private List<Long> findClustersForHostsNotOwnedByAnyManagementServer() {
        final SearchCriteria<Long> sc = ClustersForHostsNotOwnedByAnyMSSearch.create();

        final List<Long> clusters = customSearch(sc, null);
        return clusters;
    }

    /*
     * Returns a list of all cluster Ids
     */
    private List<Long> listAllClusters() {
        final SearchCriteria<Long> sc = AllClustersSearch.create();
        sc.setParameters("managed", Managed.ManagedState.Managed);

        final List<Long> clusters = _clusterDao.customSearch(sc, null);
        return clusters;
    }

    /*
     * This determines if hosts belonging to cluster(@clusterId) are up for grabs
     *
     * This is used for handling following cases:
     * 1. First host added in cluster
     * 2. During MS restart all hosts in a cluster are without any MS
     */
    private boolean canOwnCluster(final long clusterId) {
        final SearchCriteria<HostVO> sc = HostsInClusterSearch.create();
        sc.setParameters("cluster", clusterId);

        final List<HostVO> hosts = search(sc, null);
        final boolean ownCluster = (hosts == null || hosts.size() == 0);

        return ownCluster;
    }

    @Override
    public boolean updateState(final Status oldStatus, final Event event, final Status newStatus, final Host vo, final Object data) {
        // lock target row from beginning to avoid lock-promotion caused deadlock
        HostVO host = lockRow(vo.getId(), true);
        if (host == null) {
            if (event == Event.Remove && newStatus == Status.Removed) {
                host = findByIdIncludingRemoved(vo.getId());
            }
        }

        if (host == null) {
            return false;
        }
        final long oldPingTime = host.getLastPinged();

        final SearchBuilder<HostVO> sb = createSearchBuilder();
        sb.and("status", sb.entity().getStatus(), SearchCriteria.Op.EQ);
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("update", sb.entity().getUpdated(), SearchCriteria.Op.EQ);
        if (newStatus.checkManagementServer()) {
            sb.and("ping", sb.entity().getLastPinged(), SearchCriteria.Op.EQ);
            sb.and().op("nullmsid", sb.entity().getManagementServerId(), SearchCriteria.Op.NULL);
            sb.or("msid", sb.entity().getManagementServerId(), SearchCriteria.Op.EQ);
            sb.cp();
        }
        sb.done();

        final SearchCriteria<HostVO> sc = sb.create();

        sc.setParameters("status", oldStatus);
        sc.setParameters("id", host.getId());
        sc.setParameters("update", host.getUpdated());
        final long oldUpdateCount = host.getUpdated();
        if (newStatus.checkManagementServer()) {
            sc.setParameters("ping", oldPingTime);
            sc.setParameters("msid", host.getManagementServerId());
        }

        final long newUpdateCount = host.incrUpdated();
        final UpdateBuilder ub = getUpdateBuilder(host);
        ub.set(host, _statusAttr, newStatus);
        if (newStatus.updateManagementServer()) {
            if (newStatus.lostConnection()) {
                ub.set(host, _msIdAttr, null);
            } else {
                ub.set(host, _msIdAttr, host.getManagementServerId());
            }
            if (event.equals(Event.Ping) || event.equals(Event.AgentConnected)) {
                ub.set(host, _pingTimeAttr, System.currentTimeMillis() >> 10);
            }
        }
        if (event.equals(Event.ManagementServerDown)) {
            ub.set(host, _pingTimeAttr, ((System.currentTimeMillis() >> 10) - (10 * 60)));
        }
        final int result = update(ub, sc, null);
        assert result <= 1 : "How can this update " + result + " rows? ";

        if (result == 0) {
            final HostVO ho = findById(host.getId());
            assert ho != null : "How how how? : " + host.getId();

            if (status_logger.isDebugEnabled()) {

                final StringBuilder str = new StringBuilder("Unable to update host for event:").append(event.toString());
                str.append(". Name=").append(host.getName());
                str.append("; New=[status=")
                   .append(newStatus.toString())
                   .append(":msid=")
                   .append(newStatus.lostConnection() ? "null" : host.getManagementServerId())
                   .append(":lastpinged=")
                   .append(host.getLastPinged())
                   .append("]");
                str.append("; Old=[status=").append(oldStatus.toString()).append(":msid=").append(host.getManagementServerId()).append(":lastpinged=").append(oldPingTime)
                   .append("]");
                str.append("; DB=[status=")
                   .append(vo.getStatus().toString())
                   .append(":msid=")
                   .append(vo.getManagementServerId())
                   .append(":lastpinged=")
                   .append(vo.getLastPinged())
                   .append(":old update count=")
                   .append(oldUpdateCount)
                   .append("]");
                status_logger.debug(str.toString());
            } else {
                final StringBuilder msg = new StringBuilder("Agent status update: [");
                msg.append("id = " + host.getId());
                msg.append("; name = " + host.getName());
                msg.append("; old status = " + oldStatus);
                msg.append("; event = " + event);
                msg.append("; new status = " + newStatus);
                msg.append("; old update count = " + oldUpdateCount);
                msg.append("; new update count = " + newUpdateCount + "]");
                status_logger.debug(msg.toString());
            }

            if (ho.getState() == newStatus) {
                status_logger.debug("Host " + ho.getName() + " state has already been updated to " + newStatus);
                return true;
            }
        }

        return result > 0;
    }
}
