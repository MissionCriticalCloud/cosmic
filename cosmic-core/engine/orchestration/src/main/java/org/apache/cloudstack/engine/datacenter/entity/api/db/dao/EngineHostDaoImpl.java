package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

import com.cloud.host.Host;
import com.cloud.host.HostTagVO;
import com.cloud.host.Status;
import com.cloud.resource.ResourceState;
import com.cloud.utils.db.Attribute;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO;

import javax.inject.Inject;
import javax.persistence.TableGenerator;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component(value = "EngineHostDao")
@DB
@TableGenerator(name = "host_req_sq", table = "op_host", pkColumnName = "id", valueColumnName = "sequence", allocationSize = 1)
public class EngineHostDaoImpl extends GenericDaoBase<EngineHostVO, Long> implements EngineHostDao {
    private static final Logger s_logger = LoggerFactory.getLogger(EngineHostDaoImpl.class);

    private final SearchBuilder<EngineHostVO> TypePodDcStatusSearch;

    private final SearchBuilder<EngineHostVO> IdStatusSearch;
    private final SearchBuilder<EngineHostVO> TypeDcSearch;
    private final SearchBuilder<EngineHostVO> TypeDcStatusSearch;
    private final SearchBuilder<EngineHostVO> TypeClusterStatusSearch;
    private final SearchBuilder<EngineHostVO> MsStatusSearch;
    private final SearchBuilder<EngineHostVO> DcPrivateIpAddressSearch;
    private final SearchBuilder<EngineHostVO> DcStorageIpAddressSearch;

    private final SearchBuilder<EngineHostVO> GuidSearch;
    private final SearchBuilder<EngineHostVO> DcSearch;
    private final SearchBuilder<EngineHostVO> PodSearch;
    private final SearchBuilder<EngineHostVO> TypeSearch;
    private final SearchBuilder<EngineHostVO> StatusSearch;
    private final SearchBuilder<EngineHostVO> ResourceStateSearch;
    private final SearchBuilder<EngineHostVO> NameLikeSearch;
    private final SearchBuilder<EngineHostVO> NameSearch;
    private final SearchBuilder<EngineHostVO> SequenceSearch;
    private final SearchBuilder<EngineHostVO> DirectlyConnectedSearch;
    private final SearchBuilder<EngineHostVO> UnmanagedDirectConnectSearch;
    private final SearchBuilder<EngineHostVO> UnmanagedApplianceSearch;
    private final SearchBuilder<EngineHostVO> MaintenanceCountSearch;
    private final SearchBuilder<EngineHostVO> ClusterStatusSearch;
    private final SearchBuilder<EngineHostVO> TypeNameZoneSearch;
    private final SearchBuilder<EngineHostVO> AvailHypevisorInZone;

    private final SearchBuilder<EngineHostVO> DirectConnectSearch;
    private final SearchBuilder<EngineHostVO> ManagedDirectConnectSearch;
    private final SearchBuilder<EngineHostVO> ManagedRoutingServersSearch;
    private final SearchBuilder<EngineHostVO> SecondaryStorageVMSearch;
    private final GenericSearchBuilder<EngineHostVO, Long> HostsInStatusSearch;
    private final GenericSearchBuilder<EngineHostVO, Long> CountRoutingByDc;
    private final SearchBuilder<EngineHostVO> RoutingSearch;
    private final Attribute _statusAttr;
    private final Attribute _resourceStateAttr;
    private final Attribute _msIdAttr;
    private final Attribute _pingTimeAttr;
    private final SearchBuilder<EngineHostVO> StateChangeSearch;
    private final SearchBuilder<EngineHostVO> UUIDSearch;
    @Inject
    private HostDetailsDao _detailsDao;
    @Inject
    private HostTagsDao _hostTagsDao;
    @Inject
    private EngineClusterDao _clusterDao;

    public EngineHostDaoImpl() {

        MaintenanceCountSearch = createSearchBuilder();
        MaintenanceCountSearch.and("cluster", MaintenanceCountSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        MaintenanceCountSearch.and("resourceState", MaintenanceCountSearch.entity().getResourceState(), SearchCriteria.Op.IN);
        MaintenanceCountSearch.done();

        TypePodDcStatusSearch = createSearchBuilder();
        final EngineHostVO entity = TypePodDcStatusSearch.entity();
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

        GuidSearch = createSearchBuilder();
        GuidSearch.and("guid", GuidSearch.entity().getGuid(), SearchCriteria.Op.EQ);
        GuidSearch.done();

        DcSearch = createSearchBuilder();
        DcSearch.and("dc", DcSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
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
        PodSearch.and("pod", PodSearch.entity().getPodId(), SearchCriteria.Op.EQ);
        PodSearch.done();

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
        /*
         * UnmanagedDirectConnectSearch.op(SearchCriteria.Op.OR, "managementServerId",
         * UnmanagedDirectConnectSearch.entity().getManagementServerId(), SearchCriteria.Op.EQ);
         * UnmanagedDirectConnectSearch.and("lastPinged", UnmanagedDirectConnectSearch.entity().getLastPinged(),
         * SearchCriteria.Op.LTEQ); UnmanagedDirectConnectSearch.cp(); UnmanagedDirectConnectSearch.cp();
         */

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

        _statusAttr = _allAttributes.get("status");
        _msIdAttr = _allAttributes.get("managementServerId");
        _pingTimeAttr = _allAttributes.get("lastPinged");
        _resourceStateAttr = _allAttributes.get("resourceState");

        assert (_statusAttr != null && _msIdAttr != null && _pingTimeAttr != null) : "Couldn't find one of these attributes";

        UUIDSearch = createSearchBuilder();
        UUIDSearch.and("uuid", UUIDSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        UUIDSearch.done();

        StateChangeSearch = createSearchBuilder();
        StateChangeSearch.and("id", StateChangeSearch.entity().getId(), SearchCriteria.Op.EQ);
        StateChangeSearch.and("state", StateChangeSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        StateChangeSearch.done();
    }

    @Override
    public boolean updateState(final State currentState, final DataCenterResourceEntity.State.Event event, final State nextState, final DataCenterResourceEntity hostEntity,
                               final Object data) {
        final EngineHostVO vo = findById(hostEntity.getId());
        final Date oldUpdatedTime = vo.getLastUpdated();

        final SearchCriteria<EngineHostVO> sc = StateChangeSearch.create();
        sc.setParameters("id", hostEntity.getId());
        sc.setParameters("state", currentState);

        final UpdateBuilder builder = getUpdateBuilder(vo);
        builder.set(vo, "state", nextState);
        builder.set(vo, "lastUpdated", new Date());

        final int rows = update(vo, sc);

        if (rows == 0 && s_logger.isDebugEnabled()) {
            final EngineHostVO dbHost = findByIdIncludingRemoved(vo.getId());
            if (dbHost != null) {
                final StringBuilder str = new StringBuilder("Unable to update ").append(vo.toString());
                str.append(": DB Data={id=").append(dbHost.getId()).append("; state=").append(dbHost.getState()).append(";updatedTime=").append(dbHost.getLastUpdated());
                str.append(": New Data={id=")
                   .append(vo.getId())
                   .append("; state=")
                   .append(nextState)
                   .append("; event=")
                   .append(event)
                   .append("; updatedTime=")
                   .append(vo.getLastUpdated());
                str.append(": stale Data={id=")
                   .append(vo.getId())
                   .append("; state=")
                   .append(currentState)
                   .append("; event=")
                   .append(event)
                   .append("; updatedTime=")
                   .append(oldUpdatedTime);
            } else {
                s_logger.debug("Unable to update dataCenter: id=" + vo.getId() + ", as there is no such dataCenter exists in the database anymore");
            }
        }
        return rows > 0;
    }

    @Override
    public List<org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO> lockRows(
            final SearchCriteria<org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO> sc, final Filter filter, final boolean exclusive) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO> searchIncludingRemoved(
            final SearchCriteria<org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO> sc, final Filter filter, final Boolean lock, final boolean cache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<EngineHostVO> searchIncludingRemoved(final SearchCriteria<EngineHostVO> sc, final Filter filter, final Boolean lock, final boolean cache, final boolean
            enableQueryCache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EngineHostVO findOneBy(final SearchCriteria<EngineHostVO> sc) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int expunge(final SearchCriteria<EngineHostVO> sc) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO> search(
            final SearchCriteria<org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO> sc, final Filter filter) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO> search(
            final SearchCriteria<org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostVO> sc, final Filter filter, final boolean enableQueryCache) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @DB
    public boolean update(final Long hostId, final EngineHostVO host) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        final boolean persisted = super.update(hostId, host);
        if (!persisted) {
            return persisted;
        }

        saveDetails(host);
        saveHostTags(host);

        txn.commit();

        return persisted;
    }

    @Override
    @DB
    public EngineHostVO persist(final EngineHostVO host) {
        final String InsertSequenceSql = "INSERT INTO op_host(id) VALUES(?)";

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        final EngineHostVO dbHost = super.persist(host);

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

        txn.commit();

        return dbHost;
    }

    @Override
    public void loadDetails(final EngineHostVO host) {
        final Map<String, String> details = _detailsDao.findDetails(host.getId());
        host.setDetails(details);
    }

    @Override
    public void saveDetails(final EngineHostVO host) {
        final Map<String, String> details = host.getDetails();
        if (details == null) {
            return;
        }
        _detailsDao.persist(host.getId(), details);
    }

    @Override
    public void loadHostTags(final EngineHostVO host) {
        final List<String> hostTags = _hostTagsDao.gethostTags(host.getId());
        host.setHostTags(hostTags);
    }

    @Override
    public List<EngineHostVO> listByHostTag(final Host.Type type, final Long clusterId, final Long podId, final long dcId, final String hostTag) {

        final SearchBuilder<HostTagVO> hostTagSearch = _hostTagsDao.createSearchBuilder();
        final HostTagVO tagEntity = hostTagSearch.entity();
        hostTagSearch.and("tag", tagEntity.getTag(), SearchCriteria.Op.EQ);

        final SearchBuilder<EngineHostVO> hostSearch = createSearchBuilder();
        final EngineHostVO entity = hostSearch.entity();
        hostSearch.and("type", entity.getType(), SearchCriteria.Op.EQ);
        hostSearch.and("pod", entity.getPodId(), SearchCriteria.Op.EQ);
        hostSearch.and("dc", entity.getDataCenterId(), SearchCriteria.Op.EQ);
        hostSearch.and("cluster", entity.getClusterId(), SearchCriteria.Op.EQ);
        hostSearch.and("status", entity.getStatus(), SearchCriteria.Op.EQ);
        hostSearch.and("resourceState", entity.getResourceState(), SearchCriteria.Op.EQ);
        hostSearch.join("hostTagSearch", hostTagSearch, entity.getId(), tagEntity.getHostId(), JoinBuilder.JoinType.INNER);

        final SearchCriteria<EngineHostVO> sc = hostSearch.create();
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

    @Override
    public int remove(final SearchCriteria<EngineHostVO> sc) {
        // TODO Auto-generated method stub
        return 0;
    }

    protected void saveHostTags(final EngineHostVO host) {
        final List<String> hostTags = host.getHostTags();
        if (hostTags == null || (hostTags != null && hostTags.isEmpty())) {
            return;
        }
        _hostTagsDao.persist(host.getId(), hostTags);
    }
}
