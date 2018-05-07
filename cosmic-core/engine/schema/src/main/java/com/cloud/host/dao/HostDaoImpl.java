package com.cloud.host.dao;

import com.cloud.cluster.agentlb.HostTransferMapVO;
import com.cloud.cluster.agentlb.dao.HostTransferMapDao;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.gpu.dao.HostGpuGroupsDao;
import com.cloud.gpu.dao.VGPUTypesDao;
import com.cloud.host.HostTagVO;
import com.cloud.host.HostVO;
import com.cloud.info.RunningHostCountInfo;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.resource.ResourceState;
import com.cloud.legacymodel.vm.VgpuTypesInfo;
import com.cloud.model.enumeration.Event;
import com.cloud.model.enumeration.HostType;
import com.cloud.model.enumeration.ManagedState;
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
    private static final Logger status_logger = LoggerFactory.getLogger(HostStatus.class);
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

        this.MaintenanceCountSearch = createSearchBuilder();
        this.MaintenanceCountSearch.and("cluster", this.MaintenanceCountSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        this.MaintenanceCountSearch.and("resourceState", this.MaintenanceCountSearch.entity().getResourceState(), SearchCriteria.Op.IN);
        this.MaintenanceCountSearch.done();

        this.TypePodDcStatusSearch = createSearchBuilder();
        final HostVO entity = this.TypePodDcStatusSearch.entity();
        this.TypePodDcStatusSearch.and("type", entity.getType(), SearchCriteria.Op.EQ);
        this.TypePodDcStatusSearch.and("pod", entity.getPodId(), SearchCriteria.Op.EQ);
        this.TypePodDcStatusSearch.and("dc", entity.getDataCenterId(), SearchCriteria.Op.EQ);
        this.TypePodDcStatusSearch.and("cluster", entity.getClusterId(), SearchCriteria.Op.EQ);
        this.TypePodDcStatusSearch.and("status", entity.getStatus(), SearchCriteria.Op.EQ);
        this.TypePodDcStatusSearch.and("resourceState", entity.getResourceState(), SearchCriteria.Op.EQ);
        this.TypePodDcStatusSearch.done();

        this.MsStatusSearch = createSearchBuilder();
        this.MsStatusSearch.and("ms", this.MsStatusSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
        this.MsStatusSearch.and("type", this.MsStatusSearch.entity().getType(), SearchCriteria.Op.EQ);
        this.MsStatusSearch.and("resourceState", this.MsStatusSearch.entity().getResourceState(), SearchCriteria.Op.NIN);
        this.MsStatusSearch.done();

        this.TypeDcSearch = createSearchBuilder();
        this.TypeDcSearch.and("type", this.TypeDcSearch.entity().getType(), SearchCriteria.Op.EQ);
        this.TypeDcSearch.and("dc", this.TypeDcSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        this.TypeDcSearch.done();

        this.SecondaryStorageVMSearch = createSearchBuilder();
        this.SecondaryStorageVMSearch.and("type", this.SecondaryStorageVMSearch.entity().getType(), SearchCriteria.Op.EQ);
        this.SecondaryStorageVMSearch.and("dc", this.SecondaryStorageVMSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        this.SecondaryStorageVMSearch.and("status", this.SecondaryStorageVMSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        this.SecondaryStorageVMSearch.done();

        this.TypeDcStatusSearch = createSearchBuilder();
        this.TypeDcStatusSearch.and("type", this.TypeDcStatusSearch.entity().getType(), SearchCriteria.Op.EQ);
        this.TypeDcStatusSearch.and("dc", this.TypeDcStatusSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        this.TypeDcStatusSearch.and("status", this.TypeDcStatusSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        this.TypeDcStatusSearch.and("resourceState", this.TypeDcStatusSearch.entity().getResourceState(), SearchCriteria.Op.EQ);
        this.TypeDcStatusSearch.done();

        this.TypeClusterStatusSearch = createSearchBuilder();
        this.TypeClusterStatusSearch.and("type", this.TypeClusterStatusSearch.entity().getType(), SearchCriteria.Op.EQ);
        this.TypeClusterStatusSearch.and("cluster", this.TypeClusterStatusSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        this.TypeClusterStatusSearch.and("status", this.TypeClusterStatusSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        this.TypeClusterStatusSearch.and("resourceState", this.TypeClusterStatusSearch.entity().getResourceState(), SearchCriteria.Op.EQ);
        this.TypeClusterStatusSearch.done();

        this.IdStatusSearch = createSearchBuilder();
        this.IdStatusSearch.and("id", this.IdStatusSearch.entity().getId(), SearchCriteria.Op.EQ);
        this.IdStatusSearch.and("states", this.IdStatusSearch.entity().getStatus(), SearchCriteria.Op.IN);
        this.IdStatusSearch.done();

        this.DcPrivateIpAddressSearch = createSearchBuilder();
        this.DcPrivateIpAddressSearch.and("privateIpAddress", this.DcPrivateIpAddressSearch.entity().getPrivateIpAddress(), SearchCriteria.Op.EQ);
        this.DcPrivateIpAddressSearch.and("dc", this.DcPrivateIpAddressSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        this.DcPrivateIpAddressSearch.done();

        this.DcStorageIpAddressSearch = createSearchBuilder();
        this.DcStorageIpAddressSearch.and("storageIpAddress", this.DcStorageIpAddressSearch.entity().getStorageIpAddress(), SearchCriteria.Op.EQ);
        this.DcStorageIpAddressSearch.and("dc", this.DcStorageIpAddressSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        this.DcStorageIpAddressSearch.done();

        this.PublicIpAddressSearch = createSearchBuilder();
        this.PublicIpAddressSearch.and("publicIpAddress", this.PublicIpAddressSearch.entity().getPublicIpAddress(), SearchCriteria.Op.EQ);
        this.PublicIpAddressSearch.done();

        this.GuidSearch = createSearchBuilder();
        this.GuidSearch.and("guid", this.GuidSearch.entity().getGuid(), SearchCriteria.Op.EQ);
        this.GuidSearch.done();

        this.DcSearch = createSearchBuilder();
        this.DcSearch.and("dc", this.DcSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        this.DcSearch.and("type", this.DcSearch.entity().getType(), Op.EQ);
        this.DcSearch.and("status", this.DcSearch.entity().getStatus(), Op.EQ);
        this.DcSearch.and("resourceState", this.DcSearch.entity().getResourceState(), Op.EQ);
        this.DcSearch.done();

        this.ClusterStatusSearch = createSearchBuilder();
        this.ClusterStatusSearch.and("cluster", this.ClusterStatusSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        this.ClusterStatusSearch.and("status", this.ClusterStatusSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        this.ClusterStatusSearch.done();

        this.TypeNameZoneSearch = createSearchBuilder();
        this.TypeNameZoneSearch.and("name", this.TypeNameZoneSearch.entity().getName(), SearchCriteria.Op.EQ);
        this.TypeNameZoneSearch.and("type", this.TypeNameZoneSearch.entity().getType(), SearchCriteria.Op.EQ);
        this.TypeNameZoneSearch.and("zoneId", this.TypeNameZoneSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        this.TypeNameZoneSearch.done();

        this.PodSearch = createSearchBuilder();
        this.PodSearch.and("podId", this.PodSearch.entity().getPodId(), SearchCriteria.Op.EQ);
        this.PodSearch.done();

        this.ClusterSearch = createSearchBuilder();
        this.ClusterSearch.and("clusterId", this.ClusterSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        this.ClusterSearch.done();

        this.TypeSearch = createSearchBuilder();
        this.TypeSearch.and("type", this.TypeSearch.entity().getType(), SearchCriteria.Op.EQ);
        this.TypeSearch.done();

        this.StatusSearch = createSearchBuilder();
        this.StatusSearch.and("status", this.StatusSearch.entity().getStatus(), SearchCriteria.Op.IN);
        this.StatusSearch.done();

        this.ResourceStateSearch = createSearchBuilder();
        this.ResourceStateSearch.and("resourceState", this.ResourceStateSearch.entity().getResourceState(), SearchCriteria.Op.IN);
        this.ResourceStateSearch.done();

        this.NameLikeSearch = createSearchBuilder();
        this.NameLikeSearch.and("name", this.NameLikeSearch.entity().getName(), SearchCriteria.Op.LIKE);
        this.NameLikeSearch.done();

        this.NameSearch = createSearchBuilder();
        this.NameSearch.and("name", this.NameSearch.entity().getName(), SearchCriteria.Op.EQ);
        this.NameSearch.done();

        this.SequenceSearch = createSearchBuilder();
        this.SequenceSearch.and("id", this.SequenceSearch.entity().getId(), SearchCriteria.Op.EQ);
        // SequenceSearch.addRetrieve("sequence", SequenceSearch.entity().getSequence());
        this.SequenceSearch.done();

        this.DirectlyConnectedSearch = createSearchBuilder();
        this.DirectlyConnectedSearch.and("resource", this.DirectlyConnectedSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        this.DirectlyConnectedSearch.and("ms", this.DirectlyConnectedSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
        this.DirectlyConnectedSearch.and("statuses", this.DirectlyConnectedSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        this.DirectlyConnectedSearch.and("resourceState", this.DirectlyConnectedSearch.entity().getResourceState(), SearchCriteria.Op.NOTIN);
        this.DirectlyConnectedSearch.done();

        this.UnmanagedDirectConnectSearch = createSearchBuilder();
        this.UnmanagedDirectConnectSearch.and("resource", this.UnmanagedDirectConnectSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        this.UnmanagedDirectConnectSearch.and("server", this.UnmanagedDirectConnectSearch.entity().getManagementServerId(), SearchCriteria.Op.NULL);
        this.UnmanagedDirectConnectSearch.and("lastPinged", this.UnmanagedDirectConnectSearch.entity().getLastPinged(), SearchCriteria.Op.LTEQ);
        this.UnmanagedDirectConnectSearch.and("resourceStates", this.UnmanagedDirectConnectSearch.entity().getResourceState(), SearchCriteria.Op.NIN);
        this.UnmanagedDirectConnectSearch.and("clusterIn", this.UnmanagedDirectConnectSearch.entity().getClusterId(), SearchCriteria.Op.IN);
        /*
         * UnmanagedDirectConnectSearch.op(SearchCriteria.Op.OR, "managementServerId",
         * UnmanagedDirectConnectSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
         * UnmanagedDirectConnectSearch.and("lastPinged", UnmanagedDirectConnectSearch.entity().getLastPinged(),
         * SearchCriteria.Op.LTEQ); UnmanagedDirectConnectSearch.cp(); UnmanagedDirectConnectSearch.cp();
         */
        this.HostTransferSearch = this._hostTransferDao.createSearchBuilder();
        this.HostTransferSearch.and("id", this.HostTransferSearch.entity().getId(), SearchCriteria.Op.NULL);
        this.UnmanagedDirectConnectSearch.join("hostTransferSearch", this.HostTransferSearch, this.HostTransferSearch.entity().getId(), this.UnmanagedDirectConnectSearch.entity().getId(),
                JoinType.LEFTOUTER);
        this.ClusterManagedSearch = this._clusterDao.createSearchBuilder();
        this.ClusterManagedSearch.and("managed", this.ClusterManagedSearch.entity().getManagedState(), SearchCriteria.Op.EQ);
        this.UnmanagedDirectConnectSearch.join("ClusterManagedSearch", this.ClusterManagedSearch, this.ClusterManagedSearch.entity().getId(), this.UnmanagedDirectConnectSearch.entity()
                                                                                                                                                                               .getClusterId(),
                JoinType.INNER);
        this.UnmanagedDirectConnectSearch.done();

        this.DirectConnectSearch = createSearchBuilder();
        this.DirectConnectSearch.and("resource", this.DirectConnectSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        this.DirectConnectSearch.and("id", this.DirectConnectSearch.entity().getId(), SearchCriteria.Op.EQ);
        this.DirectConnectSearch.and().op("nullserver", this.DirectConnectSearch.entity().getManagementServerId(), SearchCriteria.Op.NULL);
        this.DirectConnectSearch.or("server", this.DirectConnectSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
        this.DirectConnectSearch.cp();
        this.DirectConnectSearch.done();

        this.UnmanagedApplianceSearch = createSearchBuilder();
        this.UnmanagedApplianceSearch.and("resource", this.UnmanagedApplianceSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        this.UnmanagedApplianceSearch.and("server", this.UnmanagedApplianceSearch.entity().getManagementServerId(), SearchCriteria.Op.NULL);
        this.UnmanagedApplianceSearch.and("types", this.UnmanagedApplianceSearch.entity().getType(), SearchCriteria.Op.IN);
        this.UnmanagedApplianceSearch.and("lastPinged", this.UnmanagedApplianceSearch.entity().getLastPinged(), SearchCriteria.Op.LTEQ);
        this.UnmanagedApplianceSearch.done();

        this.AvailHypevisorInZone = createSearchBuilder();
        this.AvailHypevisorInZone.and("zoneId", this.AvailHypevisorInZone.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        this.AvailHypevisorInZone.and("hostId", this.AvailHypevisorInZone.entity().getId(), SearchCriteria.Op.NEQ);
        this.AvailHypevisorInZone.and("type", this.AvailHypevisorInZone.entity().getType(), SearchCriteria.Op.EQ);
        this.AvailHypevisorInZone.groupBy(this.AvailHypevisorInZone.entity().getHypervisorType());
        this.AvailHypevisorInZone.done();

        this.HostsInStatusSearch = createSearchBuilder(Long.class);
        this.HostsInStatusSearch.selectFields(this.HostsInStatusSearch.entity().getId());
        this.HostsInStatusSearch.and("dc", this.HostsInStatusSearch.entity().getDataCenterId(), Op.EQ);
        this.HostsInStatusSearch.and("pod", this.HostsInStatusSearch.entity().getPodId(), Op.EQ);
        this.HostsInStatusSearch.and("cluster", this.HostsInStatusSearch.entity().getClusterId(), Op.EQ);
        this.HostsInStatusSearch.and("type", this.HostsInStatusSearch.entity().getType(), Op.EQ);
        this.HostsInStatusSearch.and("statuses", this.HostsInStatusSearch.entity().getStatus(), Op.IN);
        this.HostsInStatusSearch.done();

        this.CountRoutingByDc = createSearchBuilder(Long.class);
        this.CountRoutingByDc.select(null, Func.COUNT, null);
        this.CountRoutingByDc.and("dc", this.CountRoutingByDc.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        this.CountRoutingByDc.and("type", this.CountRoutingByDc.entity().getType(), SearchCriteria.Op.EQ);
        this.CountRoutingByDc.and("status", this.CountRoutingByDc.entity().getStatus(), SearchCriteria.Op.EQ);
        this.CountRoutingByDc.done();

        this.ManagedDirectConnectSearch = createSearchBuilder();
        this.ManagedDirectConnectSearch.and("resource", this.ManagedDirectConnectSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        this.ManagedDirectConnectSearch.and("server", this.ManagedDirectConnectSearch.entity().getManagementServerId(), SearchCriteria.Op.NULL);
        this.ManagedDirectConnectSearch.done();

        this.ManagedRoutingServersSearch = createSearchBuilder();
        this.ManagedRoutingServersSearch.and("server", this.ManagedRoutingServersSearch.entity().getManagementServerId(), SearchCriteria.Op.NNULL);
        this.ManagedRoutingServersSearch.and("type", this.ManagedRoutingServersSearch.entity().getType(), SearchCriteria.Op.EQ);
        this.ManagedRoutingServersSearch.done();

        this.RoutingSearch = createSearchBuilder();
        this.RoutingSearch.and("type", this.RoutingSearch.entity().getType(), SearchCriteria.Op.EQ);
        this.RoutingSearch.done();

        this.HostsForReconnectSearch = createSearchBuilder();
        this.HostsForReconnectSearch.and("resource", this.HostsForReconnectSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        this.HostsForReconnectSearch.and("server", this.HostsForReconnectSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
        this.HostsForReconnectSearch.and("lastPinged", this.HostsForReconnectSearch.entity().getLastPinged(), SearchCriteria.Op.LTEQ);
        this.HostsForReconnectSearch.and("resourceStates", this.HostsForReconnectSearch.entity().getResourceState(), SearchCriteria.Op.NIN);
        this.HostsForReconnectSearch.and("cluster", this.HostsForReconnectSearch.entity().getClusterId(), SearchCriteria.Op.NNULL);
        this.HostsForReconnectSearch.and("status", this.HostsForReconnectSearch.entity().getStatus(), SearchCriteria.Op.IN);
        this.HostsForReconnectSearch.done();

        this.ClustersOwnedByMSSearch = createSearchBuilder(Long.class);
        this.ClustersOwnedByMSSearch.select(null, Func.DISTINCT, this.ClustersOwnedByMSSearch.entity().getClusterId());
        this.ClustersOwnedByMSSearch.and("resource", this.ClustersOwnedByMSSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        this.ClustersOwnedByMSSearch.and("cluster", this.ClustersOwnedByMSSearch.entity().getClusterId(), SearchCriteria.Op.NNULL);
        this.ClustersOwnedByMSSearch.and("server", this.ClustersOwnedByMSSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
        this.ClustersOwnedByMSSearch.done();

        this.ClustersForHostsNotOwnedByAnyMSSearch = createSearchBuilder(Long.class);
        this.ClustersForHostsNotOwnedByAnyMSSearch.select(null, Func.DISTINCT, this.ClustersForHostsNotOwnedByAnyMSSearch.entity().getClusterId());
        this.ClustersForHostsNotOwnedByAnyMSSearch.and("resource", this.ClustersForHostsNotOwnedByAnyMSSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        this.ClustersForHostsNotOwnedByAnyMSSearch.and("cluster", this.ClustersForHostsNotOwnedByAnyMSSearch.entity().getClusterId(), SearchCriteria.Op.NNULL);
        this.ClustersForHostsNotOwnedByAnyMSSearch.and("server", this.ClustersForHostsNotOwnedByAnyMSSearch.entity().getManagementServerId(), SearchCriteria.Op.NULL);
        this.ClustersForHostsNotOwnedByAnyMSSearch.done();

        this.AllClustersSearch = this._clusterDao.createSearchBuilder(Long.class);
        this.AllClustersSearch.select(null, Func.NATIVE, this.AllClustersSearch.entity().getId());
        this.AllClustersSearch.and("managed", this.AllClustersSearch.entity().getManagedState(), SearchCriteria.Op.EQ);
        this.AllClustersSearch.done();

        this.HostsInClusterSearch = createSearchBuilder();
        this.HostsInClusterSearch.and("resource", this.HostsInClusterSearch.entity().getResource(), SearchCriteria.Op.NNULL);
        this.HostsInClusterSearch.and("cluster", this.HostsInClusterSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        this.HostsInClusterSearch.and("server", this.HostsInClusterSearch.entity().getManagementServerId(), SearchCriteria.Op.NNULL);
        this.HostsInClusterSearch.done();

        this.HostIdSearch = createSearchBuilder(Long.class);
        this.HostIdSearch.selectFields(this.HostIdSearch.entity().getId());
        this.HostIdSearch.and("dataCenterId", this.HostIdSearch.entity().getDataCenterId(), Op.EQ);
        this.HostIdSearch.done();

        this._statusAttr = this._allAttributes.get("status");
        this._msIdAttr = this._allAttributes.get("managementServerId");
        this._pingTimeAttr = this._allAttributes.get("lastPinged");
        this._resourceStateAttr = this._allAttributes.get("resourceState");

        assert (this._statusAttr != null && this._msIdAttr != null && this._pingTimeAttr != null) : "Couldn't find one of these attributes";
    }

    @Override
    public long countBy(final long clusterId, final ResourceState... states) {
        final SearchCriteria<HostVO> sc = this.MaintenanceCountSearch.create();

        sc.setParameters("resourceState", (Object[]) states);
        sc.setParameters("cluster", clusterId);

        final List<HostVO> hosts = listBy(sc);
        return hosts.size();
    }

    @Override
    public void markHostsAsDisconnected(final long msId, final long lastPing) {
        SearchCriteria<HostVO> sc = this.MsStatusSearch.create();
        sc.setParameters("ms", msId);

        HostVO host = createForUpdate();
        host.setLastPinged(lastPing);
        host.setDisconnectedOn(new Date());
        UpdateBuilder ub = getUpdateBuilder(host);
        ub.set(host, "status", HostStatus.Disconnected);

        update(ub, sc, null);

        sc = this.MsStatusSearch.create();
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
                        "'Disconnected', 'Connecting') and h.type not in ('ExternalLoadBalancer', 'TrafficMonitor', 'SecondaryStorage', " +
                        "'LocalSecondaryStorage', 'L2Networking') and (h.cluster_id is null or c.managed_state = 'Managed') ;";
        try (
                final TransactionLegacy txn = TransactionLegacy.currentTxn();
                final PreparedStatement pstmt = txn.prepareStatement(sql)) {
            pstmt.setLong(1, timeout);
            try (final ResultSet rs = pstmt.executeQuery()) {
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
            final SearchCriteria<HostVO> sc = this.UnmanagedDirectConnectSearch.create();
            sc.setParameters("lastPinged", lastPingSecondsAfter);
            sc.setJoinParameters("ClusterManagedSearch", "managed", ManagedState.Managed);
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
                final SearchCriteria<HostVO> sc = this.UnmanagedDirectConnectSearch.create();
                sc.setParameters("lastPinged", lastPingSecondsAfter);
                sc.setJoinParameters("ClusterManagedSearch", "managed", ManagedState.Managed);
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

        final TableGenerator tg = this._tgs.get("host_req_sq");
        assert tg != null : "how can this be wrong!";

        return s_seqFetcher.getNextSequence(Long.class, tg, hostId);
    }

    @Override
    public void loadDetails(final HostVO host) {
        final Map<String, String> details = this._detailsDao.findDetails(host.getId());
        host.setDetails(details);
    }

    @Override
    public void saveDetails(final HostVO host) {
        final Map<String, String> details = host.getDetails();
        if (details == null) {
            return;
        }
        this._detailsDao.persist(host.getId(), details);
    }

    @Override
    public void loadHostTags(final HostVO host) {
        final List<String> hostTags = this._hostTagsDao.gethostTags(host.getId());
        host.setHostTags(hostTags);
    }

    @Override
    public List<HostVO> listByHostTag(final HostType type, final Long clusterId, final Long podId, final long dcId, final String hostTag) {

        final SearchBuilder<HostTagVO> hostTagSearch = this._hostTagsDao.createSearchBuilder();
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
        sc.setParameters("status", HostStatus.Up.toString());
        sc.setParameters("resourceState", ResourceState.Enabled.toString());

        return listBy(sc);
    }

    @Override
    public long countRoutingHostsByDataCenter(final long dcId) {
        final SearchCriteria<Long> sc = this.CountRoutingByDc.create();
        sc.setParameters("dc", dcId);
        sc.setParameters("type", HostType.Routing);
        sc.setParameters("status", HostStatus.Up.toString());
        return customSearch(sc, null).get(0);
    }

    @Override
    @DB
    public List<HostVO> findAndUpdateApplianceToLoad(final long lastPingSecondsAfter, final long managementServerId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();

        txn.start();
        final SearchCriteria<HostVO> sc = this.UnmanagedApplianceSearch.create();
        sc.setParameters("lastPinged", lastPingSecondsAfter);
        sc.setParameters("types", HostType.ExternalDhcp, HostType.ExternalLoadBalancer, HostType.TrafficMonitor,
                HostType.L2Networking);
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
        this._hostTagsDao.persist(host.getId(), hostTags);
    }

    protected void saveGpuRecords(final HostVO host) {
        final HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails = host.getGpuGroupDetails();
        if (groupDetails != null) {
            // Create/Update GPU group entries
            this._hostGpuGroupsDao.persist(host.getId(), new ArrayList<>(groupDetails.keySet()));
            // Create/Update VGPU types entries
            this._vgpuTypesDao.persist(host.getId(), groupDetails);
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
        ub.set(host, this._resourceStateAttr, newState);
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
        final SearchCriteria<HostVO> sc = this.GuidSearch.create("guid", guid);
        return findOneBy(sc);
    }

    @Override
    public HostVO findByTypeNameAndZoneId(final long zoneId, final String name, final HostType type) {
        final SearchCriteria<HostVO> sc = this.TypeNameZoneSearch.create();
        sc.setParameters("type", type);
        sc.setParameters("name", name);
        sc.setParameters("zoneId", zoneId);
        return findOneBy(sc);
    }

    @Override
    public List<HostVO> findHypervisorHostInCluster(final long clusterId) {
        final SearchCriteria<HostVO> sc = this.TypeClusterStatusSearch.create();
        sc.setParameters("type", HostType.Routing);
        sc.setParameters("cluster", clusterId);
        sc.setParameters("status", HostStatus.Up);
        sc.setParameters("resourceState", ResourceState.Enabled);

        return listBy(sc);
    }

    @Override
    public List<HostVO> listAllUpAndEnabledNonHAHosts(final HostType type, final Long clusterId, final Long podId, final long dcId, final String haTag) {
        SearchBuilder<HostTagVO> hostTagSearch = null;
        if (haTag != null && !haTag.isEmpty()) {
            hostTagSearch = this._hostTagsDao.createSearchBuilder();
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
        sc.setParameters("status", HostStatus.Up);
        sc.setParameters("resourceState", ResourceState.Enabled);

        return listBy(sc);
    }

    @Override
    public List<HostVO> findByPodId(final Long podId) {
        final SearchCriteria<HostVO> sc = this.PodSearch.create();
        sc.setParameters("podId", podId);
        return listBy(sc);
    }

    @Override
    public List<HostVO> findByClusterId(final Long clusterId) {
        final SearchCriteria<HostVO> sc = this.ClusterSearch.create();
        sc.setParameters("clusterId", clusterId);
        return listBy(sc);
    }

    @Override
    public List<HostVO> listByDataCenterId(final long id) {
        final SearchCriteria<HostVO> sc = this.DcSearch.create();
        sc.setParameters("dc", id);
        sc.setParameters("status", HostStatus.Up);
        sc.setParameters("type", HostType.Routing);
        sc.setParameters("resourceState", ResourceState.Enabled);

        return listBy(sc);
    }

    @Override
    public List<Long> listAllHosts(final long zoneId) {
        final SearchCriteria<Long> sc = this.HostIdSearch.create();
        sc.addAnd("dataCenterId", SearchCriteria.Op.EQ, zoneId);
        return customSearch(sc, null);
    }

    @Override
    public HostVO findByPublicIp(final String publicIp) {
        final SearchCriteria<HostVO> sc = this.PublicIpAddressSearch.create();
        sc.setParameters("publicIpAddress", publicIp);
        return findOneBy(sc);
    }

    @Override
    public List<HostVO> listByType(final HostType type) {
        final SearchCriteria<HostVO> sc = this.TypeSearch.create();
        sc.setParameters("type", type);
        return listBy(sc);
    }

    /*
     * Find hosts which is in Disconnected, Down, Alert and ping timeout and server is not null, set server to null
     */
    private void resetHosts(final long managementServerId, final long lastPingSecondsAfter) {
        final SearchCriteria<HostVO> sc = this.HostsForReconnectSearch.create();
        sc.setParameters("server", managementServerId);
        sc.setParameters("lastPinged", lastPingSecondsAfter);
        sc.setParameters("status", HostStatus.Disconnected, HostStatus.Down, HostStatus.Alert);

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
        final SearchCriteria<Long> sc = this.ClustersOwnedByMSSearch.create();
        sc.setParameters("server", managementServerId);

        final List<Long> clusters = customSearch(sc, null);
        return clusters;
    }

    /*
     * Returns clusters based on the list of hosts not owned by any MS
     */
    private List<Long> findClustersForHostsNotOwnedByAnyManagementServer() {
        final SearchCriteria<Long> sc = this.ClustersForHostsNotOwnedByAnyMSSearch.create();

        final List<Long> clusters = customSearch(sc, null);
        return clusters;
    }

    /*
     * Returns a list of all cluster Ids
     */
    private List<Long> listAllClusters() {
        final SearchCriteria<Long> sc = this.AllClustersSearch.create();
        sc.setParameters("managed", ManagedState.Managed);

        final List<Long> clusters = this._clusterDao.customSearch(sc, null);
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
        final SearchCriteria<HostVO> sc = this.HostsInClusterSearch.create();
        sc.setParameters("cluster", clusterId);

        final List<HostVO> hosts = search(sc, null);
        final boolean ownCluster = (hosts == null || hosts.size() == 0);

        return ownCluster;
    }

    @Override
    public boolean updateState(final HostStatus oldStatus, final Event event, final HostStatus newStatus, final Host vo, final Object data) {
        // lock target row from beginning to avoid lock-promotion caused deadlock
        HostVO host = lockRow(vo.getId(), true);
        if (host == null) {
            if (event == Event.Remove && newStatus == HostStatus.Removed) {
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
        ub.set(host, this._statusAttr, newStatus);
        if (newStatus.updateManagementServer()) {
            if (newStatus.lostConnection()) {
                ub.set(host, this._msIdAttr, null);
            } else {
                ub.set(host, this._msIdAttr, host.getManagementServerId());
            }
            if (event.equals(Event.Ping) || event.equals(Event.AgentConnected)) {
                ub.set(host, this._pingTimeAttr, System.currentTimeMillis() >> 10);
            }
        }
        if (event.equals(Event.ManagementServerDown)) {
            ub.set(host, this._pingTimeAttr, ((System.currentTimeMillis() >> 10) - (10 * 60)));
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
