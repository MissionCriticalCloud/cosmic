package com.cloud.vm.dao;

import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.utils.DateUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Attribute;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.JoinBuilder.JoinType;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.NicVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Event;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachine.Type;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VMInstanceDaoImpl extends GenericDaoBase<VMInstanceVO, Long> implements VMInstanceDao {

    private static final Logger s_logger = LoggerFactory.getLogger(VMInstanceDaoImpl.class);

    private static final int MAX_CONSECUTIVE_SAME_STATE_UPDATE_COUNT = 3;
    private static final String ORDER_CLUSTERS_NUMBER_OF_VMS_FOR_ACCOUNT_PART1 = "SELECT host.cluster_id, SUM(IF(vm.state IN ('Starting', 'Running') AND vm.account_id = ?, 1, 0)) " +
            "FROM `cloud`.`host` host LEFT JOIN `cloud`.`vm_instance` vm ON host.id = vm.host_id WHERE ";
    private static final String ORDER_CLUSTERS_NUMBER_OF_VMS_FOR_ACCOUNT_PART2 = " AND host.type = 'Routing' AND host.removed is null GROUP BY host.cluster_id " +
            "ORDER BY 2 ASC ";
    private static final String ORDER_PODS_NUMBER_OF_VMS_FOR_ACCOUNT = "SELECT pod.id, SUM(IF(vm.state IN ('Starting', 'Running') AND vm.account_id = ?, 1, 0)) FROM `cloud`.`" +
            "host_pod_ref` pod LEFT JOIN `cloud`.`vm_instance` vm ON pod.id = vm.pod_id WHERE pod.data_center_id = ? AND pod.removed is null "
            + " GROUP BY pod.id ORDER BY 2 ASC ";
    private static final String ORDER_HOSTS_NUMBER_OF_VMS_FOR_ACCOUNT =
            "SELECT host.id, SUM(IF(vm.state IN ('Starting', 'Running') AND vm.account_id = ?, 1, 0)) FROM `cloud`.`host` host LEFT JOIN `cloud`.`vm_instance` vm ON host.id = vm.host_id " +
                    "WHERE host.data_center_id = ? AND host.type = 'Routing' AND host.removed is null ";
    private static final String ORDER_HOSTS_NUMBER_OF_VMS_FOR_ACCOUNT_PART2 = " GROUP BY host.id ORDER BY 2 ASC ";
    private static final String COUNT_VMS_BASED_ON_VGPU_TYPES1 =
            "SELECT pci, type, SUM(vmcount) FROM (SELECT MAX(IF(offering.name = 'pciDevice',value,'')) AS pci, MAX(IF(offering.name = 'vgpuType', value,'')) " +
                    "AS type, COUNT(DISTINCT vm.id) AS vmcount FROM service_offering_details offering INNER JOIN vm_instance vm ON offering.service_offering_id = vm" +
                    ".service_offering_id " +
                    "INNER JOIN `cloud`.`host` ON vm.host_id = host.id WHERE vm.state = 'Running' AND host.data_center_id = ? ";
    private static final String COUNT_VMS_BASED_ON_VGPU_TYPES2 =
            "GROUP BY offering.service_offering_id) results GROUP BY pci, type";

    private SearchBuilder<VMInstanceVO> _vmClusterSearch;
    private SearchBuilder<VMInstanceVO> _lhvmClusterSearch;
    private SearchBuilder<VMInstanceVO> _allFieldsSearch;
    private SearchBuilder<VMInstanceVO> _zoneTemplateNonExpungedSearch;
    private SearchBuilder<VMInstanceVO> _nameLikeSearch;
    private SearchBuilder<VMInstanceVO> _stateChangeSearch;
    private SearchBuilder<VMInstanceVO> _transitionSearch;
    private SearchBuilder<VMInstanceVO> _typesSearch;
    private SearchBuilder<VMInstanceVO> _idTypesSearch;
    private SearchBuilder<VMInstanceVO> _hostIdTypesSearch;
    private SearchBuilder<VMInstanceVO> _hostIdStatesSearch;
    private SearchBuilder<VMInstanceVO> _hostIdUpTypesSearch;
    private SearchBuilder<VMInstanceVO> _hostUpSearch;
    private SearchBuilder<VMInstanceVO> _instanceNameSearch;
    private SearchBuilder<VMInstanceVO> _hostNameSearch;
    private SearchBuilder<VMInstanceVO> _hostNameAndZoneSearch;
    private GenericSearchBuilder<VMInstanceVO, Long> _findIdsOfVirtualRoutersByAccount;
    private GenericSearchBuilder<VMInstanceVO, Long> _countActiveByHost;
    private GenericSearchBuilder<VMInstanceVO, Long> _countStartingOrRunningByAccount;
    private SearchBuilder<VMInstanceVO> _networkTypeSearch;
    private GenericSearchBuilder<VMInstanceVO, String> _distinctHostNameSearch;
    private SearchBuilder<VMInstanceVO> _hostAndStateSearch;
    private SearchBuilder<VMInstanceVO> _startingWithNoHostSearch;
    private Attribute _updateTimeAttr;

    @Inject
    private HostDao _hostDao;
    @Inject
    private ResourceTagDao _tagsDao;
    @Inject
    private NicDao _nicDao;

    public VMInstanceDaoImpl() {
    }

    @PostConstruct
    protected void init() {
        _vmClusterSearch = createSearchBuilder();
        final SearchBuilder<HostVO> hostSearch = _hostDao.createSearchBuilder();
        _vmClusterSearch.join("hostSearch", hostSearch, hostSearch.entity().getId(), _vmClusterSearch.entity().getHostId(), JoinType.INNER);
        hostSearch.and("clusterId", hostSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        _vmClusterSearch.done();

        _lhvmClusterSearch = createSearchBuilder();
        final SearchBuilder<HostVO> hostSearch1 = _hostDao.createSearchBuilder();
        _lhvmClusterSearch.join("hostSearch1", hostSearch1, hostSearch1.entity().getId(), _lhvmClusterSearch.entity().getLastHostId(), JoinType.INNER);
        _lhvmClusterSearch.and("hostid", _lhvmClusterSearch.entity().getHostId(), Op.NULL);
        hostSearch1.and("clusterId", hostSearch1.entity().getClusterId(), SearchCriteria.Op.EQ);
        _lhvmClusterSearch.done();

        _allFieldsSearch = createSearchBuilder();
        _allFieldsSearch.and("host", _allFieldsSearch.entity().getHostId(), Op.EQ);
        _allFieldsSearch.and("lastHost", _allFieldsSearch.entity().getLastHostId(), Op.EQ);
        _allFieldsSearch.and("state", _allFieldsSearch.entity().getState(), Op.EQ);
        _allFieldsSearch.and("zone", _allFieldsSearch.entity().getDataCenterId(), Op.EQ);
        _allFieldsSearch.and("pod", _allFieldsSearch.entity().getPodIdToDeployIn(), Op.EQ);
        _allFieldsSearch.and("type", _allFieldsSearch.entity().getType(), Op.EQ);
        _allFieldsSearch.and("account", _allFieldsSearch.entity().getAccountId(), Op.EQ);
        _allFieldsSearch.done();

        _zoneTemplateNonExpungedSearch = createSearchBuilder();
        _zoneTemplateNonExpungedSearch.and("zone", _zoneTemplateNonExpungedSearch.entity().getDataCenterId(), Op.EQ);
        _zoneTemplateNonExpungedSearch.and("template", _zoneTemplateNonExpungedSearch.entity().getTemplateId(), Op.EQ);
        _zoneTemplateNonExpungedSearch.and("state", _zoneTemplateNonExpungedSearch.entity().getState(), Op.NEQ);
        _zoneTemplateNonExpungedSearch.done();

        _nameLikeSearch = createSearchBuilder();
        _nameLikeSearch.and("name", _nameLikeSearch.entity().getHostName(), Op.LIKE);
        _nameLikeSearch.done();

        _stateChangeSearch = createSearchBuilder();
        _stateChangeSearch.and("id", _stateChangeSearch.entity().getId(), Op.EQ);
        _stateChangeSearch.and("states", _stateChangeSearch.entity().getState(), Op.EQ);
        _stateChangeSearch.and("host", _stateChangeSearch.entity().getHostId(), Op.EQ);
        _stateChangeSearch.and("update", _stateChangeSearch.entity().getUpdated(), Op.EQ);
        _stateChangeSearch.done();

        _transitionSearch = createSearchBuilder();
        _transitionSearch.and("updateTime", _transitionSearch.entity().getUpdateTime(), Op.LT);
        _transitionSearch.and("states", _transitionSearch.entity().getState(), Op.IN);
        _transitionSearch.done();

        _typesSearch = createSearchBuilder();
        _typesSearch.and("types", _typesSearch.entity().getType(), Op.IN);
        _typesSearch.done();

        _idTypesSearch = createSearchBuilder();
        _idTypesSearch.and("id", _idTypesSearch.entity().getId(), Op.EQ);
        _idTypesSearch.and("types", _idTypesSearch.entity().getType(), Op.IN);
        _idTypesSearch.done();

        _hostIdTypesSearch = createSearchBuilder();
        _hostIdTypesSearch.and("hostid", _hostIdTypesSearch.entity().getHostId(), Op.EQ);
        _hostIdTypesSearch.and("types", _hostIdTypesSearch.entity().getType(), Op.IN);
        _hostIdTypesSearch.done();

        _hostIdStatesSearch = createSearchBuilder();
        _hostIdStatesSearch.and("hostId", _hostIdStatesSearch.entity().getHostId(), Op.EQ);
        _hostIdStatesSearch.and("states", _hostIdStatesSearch.entity().getState(), Op.IN);
        _hostIdStatesSearch.done();

        _hostIdUpTypesSearch = createSearchBuilder();
        _hostIdUpTypesSearch.and("hostid", _hostIdUpTypesSearch.entity().getHostId(), Op.EQ);
        _hostIdUpTypesSearch.and("types", _hostIdUpTypesSearch.entity().getType(), Op.IN);
        _hostIdUpTypesSearch.and("states", _hostIdUpTypesSearch.entity().getState(), Op.NIN);
        _hostIdUpTypesSearch.done();

        _hostUpSearch = createSearchBuilder();
        _hostUpSearch.and("host", _hostUpSearch.entity().getHostId(), Op.EQ);
        _hostUpSearch.and("states", _hostUpSearch.entity().getState(), Op.IN);
        _hostUpSearch.done();

        _instanceNameSearch = createSearchBuilder();
        _instanceNameSearch.and("instanceName", _instanceNameSearch.entity().getInstanceName(), Op.EQ);
        _instanceNameSearch.done();

        _hostNameSearch = createSearchBuilder();
        _hostNameSearch.and("hostName", _hostNameSearch.entity().getHostName(), Op.EQ);
        _hostNameSearch.done();

        _hostNameAndZoneSearch = createSearchBuilder();
        _hostNameAndZoneSearch.and("hostName", _hostNameAndZoneSearch.entity().getHostName(), Op.EQ);
        _hostNameAndZoneSearch.and("zone", _hostNameAndZoneSearch.entity().getDataCenterId(), Op.EQ);
        _hostNameAndZoneSearch.done();

        _findIdsOfVirtualRoutersByAccount = createSearchBuilder(Long.class);
        _findIdsOfVirtualRoutersByAccount.selectFields(_findIdsOfVirtualRoutersByAccount.entity().getId());
        _findIdsOfVirtualRoutersByAccount.and("account", _findIdsOfVirtualRoutersByAccount.entity().getAccountId(), SearchCriteria.Op.EQ);
        _findIdsOfVirtualRoutersByAccount.and("type", _findIdsOfVirtualRoutersByAccount.entity().getType(), SearchCriteria.Op.EQ);
        _findIdsOfVirtualRoutersByAccount.and("state", _findIdsOfVirtualRoutersByAccount.entity().getState(), SearchCriteria.Op.NIN);
        _findIdsOfVirtualRoutersByAccount.done();

        _countActiveByHost = createSearchBuilder(Long.class);
        _countActiveByHost.select(null, Func.COUNT, null);
        _countActiveByHost.and("host", _countActiveByHost.entity().getHostId(), SearchCriteria.Op.EQ);
        _countActiveByHost.and("state", _countActiveByHost.entity().getState(), SearchCriteria.Op.IN);
        _countActiveByHost.done();

        _countStartingOrRunningByAccount = createSearchBuilder(Long.class);
        _countStartingOrRunningByAccount.select(null, Func.COUNT, null);
        _countStartingOrRunningByAccount.and("account", _countStartingOrRunningByAccount.entity().getAccountId(), SearchCriteria.Op.EQ);
        _countStartingOrRunningByAccount.and("states", _countStartingOrRunningByAccount.entity().getState(), SearchCriteria.Op.IN);
        _countStartingOrRunningByAccount.done();

        _hostAndStateSearch = createSearchBuilder();
        _hostAndStateSearch.and("host", _hostAndStateSearch.entity().getHostId(), Op.EQ);
        _hostAndStateSearch.and("states", _hostAndStateSearch.entity().getState(), Op.IN);
        _hostAndStateSearch.done();

        _startingWithNoHostSearch = createSearchBuilder();
        _startingWithNoHostSearch.and("state", _startingWithNoHostSearch.entity().getState(), Op.EQ);
        _startingWithNoHostSearch.and("host", _startingWithNoHostSearch.entity().getHostId(), Op.NULL);
        _startingWithNoHostSearch.done();

        _updateTimeAttr = _allAttributes.get("updateTime");
        assert _updateTimeAttr != null : "Couldn't get this updateTime attribute";

        final SearchBuilder<NicVO> nicSearch = _nicDao.createSearchBuilder();
        nicSearch.and("networkId", nicSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);

        _distinctHostNameSearch = createSearchBuilder(String.class);
        _distinctHostNameSearch.selectFields(_distinctHostNameSearch.entity().getHostName());

        _distinctHostNameSearch.and("types", _distinctHostNameSearch.entity().getType(), SearchCriteria.Op.IN);
        _distinctHostNameSearch.and("removed", _distinctHostNameSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        _distinctHostNameSearch.join("nicSearch", nicSearch, _distinctHostNameSearch.entity().getId(), nicSearch.entity().getInstanceId(), JoinBuilder.JoinType.INNER);
        _distinctHostNameSearch.done();
    }

    @Override
    public List<VMInstanceVO> listByHostId(final long hostid) {
        final SearchCriteria<VMInstanceVO> sc = _allFieldsSearch.create();
        sc.setParameters("host", hostid);

        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listByZoneId(final long zoneId) {
        final SearchCriteria<VMInstanceVO> sc = _allFieldsSearch.create();
        sc.setParameters("zone", zoneId);

        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listByPodId(final long podId) {
        final SearchCriteria<VMInstanceVO> sc = _allFieldsSearch.create();
        sc.setParameters("pod", podId);
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listNonExpungedByZoneAndTemplate(final long zoneId, final long templateId) {
        final SearchCriteria<VMInstanceVO> sc = _zoneTemplateNonExpungedSearch.create();

        sc.setParameters("zone", zoneId);
        sc.setParameters("template", templateId);
        sc.setParameters("state", State.Expunging);

        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> findVMInstancesLike(final String name) {
        final SearchCriteria<VMInstanceVO> sc = _nameLikeSearch.create();
        sc.setParameters("name", "%" + name + "%");
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> findVMInTransition(final Date time, final State... states) {
        final SearchCriteria<VMInstanceVO> sc = _transitionSearch.create();

        sc.setParameters("states", (Object[]) states);
        sc.setParameters("updateTime", time);

        return search(sc, null);
    }

    @Override
    public List<VMInstanceVO> listByHostAndState(final long hostId, final State... states) {
        final SearchCriteria<VMInstanceVO> sc = _hostIdStatesSearch.create();
        sc.setParameters("hostId", hostId);
        sc.setParameters("states", (Object[]) states);

        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listByTypes(final Type... types) {
        final SearchCriteria<VMInstanceVO> sc = _typesSearch.create();
        sc.setParameters("types", (Object[]) types);
        return listBy(sc);
    }

    @Override
    public VMInstanceVO findByIdTypes(final long id, final Type... types) {
        final SearchCriteria<VMInstanceVO> sc = _idTypesSearch.create();
        sc.setParameters("id", id);
        sc.setParameters("types", (Object[]) types);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public VMInstanceVO findVMByInstanceName(final String name) {
        final SearchCriteria<VMInstanceVO> sc = _instanceNameSearch.create();
        sc.setParameters("instanceName", name);
        return findOneBy(sc);
    }

    @Override
    public VMInstanceVO findVMByHostName(final String hostName) {
        final SearchCriteria<VMInstanceVO> sc = _hostNameSearch.create();
        sc.setParameters("hostName", hostName);
        return findOneBy(sc);
    }

    @Override
    public void updateProxyId(final long id, final Long proxyId, final Date time) {
        final VMInstanceVO vo = createForUpdate();
        vo.setProxyId(proxyId);
        vo.setProxyAssignTime(time);
        update(id, vo);
    }

    @Override
    public List<VMInstanceVO> listByHostIdTypes(final long hostid, final Type... types) {
        final SearchCriteria<VMInstanceVO> sc = _hostIdTypesSearch.create();
        sc.setParameters("hostid", hostid);
        sc.setParameters("types", (Object[]) types);
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listUpByHostIdTypes(final long hostid, final Type... types) {
        final SearchCriteria<VMInstanceVO> sc = _hostIdUpTypesSearch.create();
        sc.setParameters("hostid", hostid);
        sc.setParameters("types", (Object[]) types);
        sc.setParameters("states", new Object[]{State.Destroyed, State.Stopped, State.Expunging});
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listByZoneIdAndType(final long zoneId, final VirtualMachine.Type type) {
        final SearchCriteria<VMInstanceVO> sc = _allFieldsSearch.create();
        sc.setParameters("zone", zoneId);
        sc.setParameters("type", type.toString());
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listUpByHostId(final Long hostId) {
        final SearchCriteria<VMInstanceVO> sc = _hostUpSearch.create();
        sc.setParameters("host", hostId);
        sc.setParameters("states", new Object[]{State.Starting, State.Running});
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listByLastHostId(final Long hostId) {
        final SearchCriteria<VMInstanceVO> sc = _allFieldsSearch.create();
        sc.setParameters("lastHost", hostId);
        sc.setParameters("state", State.Stopped);
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listByTypeAndState(final VirtualMachine.Type type, final State state) {
        final SearchCriteria<VMInstanceVO> sc = _allFieldsSearch.create();
        sc.setParameters("type", type);
        sc.setParameters("state", state);
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listByAccountId(final long accountId) {
        final SearchCriteria<VMInstanceVO> sc = _allFieldsSearch.create();
        sc.setParameters("account", accountId);
        return listBy(sc);
    }

    @Override
    public List<Long> findIdsOfAllocatedVirtualRoutersForAccount(final long accountId) {
        final SearchCriteria<Long> sc = _findIdsOfVirtualRoutersByAccount.create();
        sc.setParameters("account", accountId);
        sc.setParameters("type", VirtualMachine.Type.DomainRouter);
        sc.setParameters("state", new Object[]{State.Destroyed, State.Error, State.Expunging});
        return customSearch(sc, null);
    }

    @Override
    public List<VMInstanceVO> listByClusterId(final long clusterId) {
        final SearchCriteria<VMInstanceVO> sc = _vmClusterSearch.create();
        sc.setJoinParameters("hostSearch", "clusterId", clusterId);
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listLHByClusterId(final long clusterId) {
        final SearchCriteria<VMInstanceVO> sc = _lhvmClusterSearch.create();
        sc.setJoinParameters("hostSearch1", "clusterId", clusterId);
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listVmsMigratingFromHost(final Long hostId) {
        final SearchCriteria<VMInstanceVO> sc = _allFieldsSearch.create();
        sc.setParameters("lastHost", hostId);
        sc.setParameters("state", State.Migrating);
        return listBy(sc);
    }

    @Override
    public Long countActiveByHostId(final long hostId) {
        final SearchCriteria<Long> sc = _countActiveByHost.create();
        sc.setParameters("host", hostId);
        sc.setParameters("state", State.Running, State.Starting, State.Stopping, State.Migrating);
        return customSearch(sc, null).get(0);
    }

    @Override
    public Pair<List<Long>, Map<Long, Double>> listClusterIdsInZoneByVmCount(final long zoneId, final long accountId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final List<Long> result = new ArrayList<>();
        final Map<Long, Double> clusterVmCountMap = new HashMap<>();

        final StringBuilder sql = new StringBuilder(ORDER_CLUSTERS_NUMBER_OF_VMS_FOR_ACCOUNT_PART1);
        sql.append("host.data_center_id = ?");
        sql.append(ORDER_CLUSTERS_NUMBER_OF_VMS_FOR_ACCOUNT_PART2);
        try {
            pstmt = txn.prepareAutoCloseStatement(sql.toString());
            pstmt.setLong(1, accountId);
            pstmt.setLong(2, zoneId);

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                final Long clusterId = rs.getLong(1);
                result.add(clusterId);
                clusterVmCountMap.put(clusterId, rs.getDouble(2));
            }
            return new Pair<>(result, clusterVmCountMap);
        } catch (final SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + sql, e);
        }
    }

    @Override
    public Pair<List<Long>, Map<Long, Double>> listClusterIdsInPodByVmCount(final long podId, final long accountId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final List<Long> result = new ArrayList<>();
        final Map<Long, Double> clusterVmCountMap = new HashMap<>();

        final StringBuilder sql = new StringBuilder(ORDER_CLUSTERS_NUMBER_OF_VMS_FOR_ACCOUNT_PART1);
        sql.append("host.pod_id = ?");
        sql.append(ORDER_CLUSTERS_NUMBER_OF_VMS_FOR_ACCOUNT_PART2);
        try {
            pstmt = txn.prepareAutoCloseStatement(sql.toString());
            pstmt.setLong(1, accountId);
            pstmt.setLong(2, podId);

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                final Long clusterId = rs.getLong(1);
                result.add(clusterId);
                clusterVmCountMap.put(clusterId, rs.getDouble(2));
            }
            return new Pair<>(result, clusterVmCountMap);
        } catch (final SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + sql, e);
        }
    }

    @Override
    public Pair<List<Long>, Map<Long, Double>> listPodIdsInZoneByVmCount(final long dataCenterId, final long accountId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final List<Long> result = new ArrayList<>();
        final Map<Long, Double> podVmCountMap = new HashMap<>();
        try {
            final String sql = ORDER_PODS_NUMBER_OF_VMS_FOR_ACCOUNT;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, accountId);
            pstmt.setLong(2, dataCenterId);

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                final Long podId = rs.getLong(1);
                result.add(podId);
                podVmCountMap.put(podId, rs.getDouble(2));
            }
            return new Pair<>(result, podVmCountMap);
        } catch (final SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + ORDER_PODS_NUMBER_OF_VMS_FOR_ACCOUNT, e);
        }
    }

    @Override
    public List<Long> listHostIdsByVmCount(final long dcId, final Long podId, final Long clusterId, final long accountId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final List<Long> result = new ArrayList<>();
        try {
            String sql = ORDER_HOSTS_NUMBER_OF_VMS_FOR_ACCOUNT;
            if (podId != null) {
                sql = sql + " AND host.pod_id = ? ";
            }

            if (clusterId != null) {
                sql = sql + " AND host.cluster_id = ? ";
            }

            sql = sql + ORDER_HOSTS_NUMBER_OF_VMS_FOR_ACCOUNT_PART2;

            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, accountId);
            pstmt.setLong(2, dcId);
            if (podId != null) {
                pstmt.setLong(3, podId);
            }
            if (clusterId != null) {
                pstmt.setLong(4, clusterId);
            }

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
            return result;
        } catch (final SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + ORDER_PODS_NUMBER_OF_VMS_FOR_ACCOUNT, e);
        }
    }

    @Override
    public Long countStartingOrRunningByAccount(final long accountId) {
        final SearchCriteria<Long> sc = _countStartingOrRunningByAccount.create();
        sc.setParameters("account", accountId);
        sc.setParameters("states", State.Starting, State.Running);
        return customSearch(sc, null).get(0);
    }

    @Override
    public List<VMInstanceVO> listNonRemovedVmsByTypeAndNetwork(final long networkId, final VirtualMachine.Type... types) {
        if (_networkTypeSearch == null) {

            final SearchBuilder<NicVO> nicSearch = _nicDao.createSearchBuilder();
            nicSearch.and("networkId", nicSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);

            _networkTypeSearch = createSearchBuilder();
            _networkTypeSearch.and("types", _networkTypeSearch.entity().getType(), SearchCriteria.Op.IN);
            _networkTypeSearch.and("removed", _networkTypeSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
            _networkTypeSearch.join("nicSearch", nicSearch, _networkTypeSearch.entity().getId(), nicSearch.entity().getInstanceId(), JoinBuilder.JoinType.INNER);
            _networkTypeSearch.done();
        }

        final SearchCriteria<VMInstanceVO> sc = _networkTypeSearch.create();
        if (types != null && types.length != 0) {
            sc.setParameters("types", (Object[]) types);
        }
        sc.setJoinParameters("nicSearch", "networkId", networkId);

        return listBy(sc);
    }

    @Override
    public List<String> listDistinctHostNames(final long networkId, final VirtualMachine.Type... types) {
        final SearchCriteria<String> sc = _distinctHostNameSearch.create();
        if (types != null && types.length != 0) {
            sc.setParameters("types", (Object[]) types);
        }
        sc.setJoinParameters("nicSearch", "networkId", networkId);

        return customSearch(sc, null);
    }

    @Override
    public List<VMInstanceVO> findByHostInStates(final Long hostId, final State... states) {
        final SearchCriteria<VMInstanceVO> sc = _hostAndStateSearch.create();
        sc.setParameters("host", hostId);
        sc.setParameters("states", (Object[]) states);
        return listBy(sc);
    }

    @Override
    public List<VMInstanceVO> listStartingWithNoHostId() {
        final SearchCriteria<VMInstanceVO> sc = _startingWithNoHostSearch.create();
        sc.setParameters("state", State.Starting);
        return listBy(sc);
    }

    @Override
    public boolean updatePowerState(final long instanceId, final long powerHostId, final VirtualMachine.PowerState powerState) {
        return Transaction.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(final TransactionStatus status) {
                boolean needToUpdate = false;
                final VMInstanceVO instance = findById(instanceId);
                if (instance != null) {
                    final Long savedPowerHostId = instance.getPowerHostId();
                    if (instance.getPowerState() != powerState || savedPowerHostId == null
                            || savedPowerHostId.longValue() != powerHostId) {
                        instance.setPowerState(powerState);
                        instance.setPowerHostId(powerHostId);
                        instance.setPowerStateUpdateCount(1);
                        instance.setPowerStateUpdateTime(DateUtil.currentGMTTime());
                        needToUpdate = true;
                        update(instanceId, instance);
                    } else {
                        // to reduce DB updates, consecutive same state update for more than 3 times
                        if (instance.getPowerStateUpdateCount() < MAX_CONSECUTIVE_SAME_STATE_UPDATE_COUNT) {
                            instance.setPowerStateUpdateCount(instance.getPowerStateUpdateCount() + 1);
                            instance.setPowerStateUpdateTime(DateUtil.currentGMTTime());
                            needToUpdate = true;
                            update(instanceId, instance);
                        }
                    }
                }
                return needToUpdate;
            }
        });
    }

    @Override
    public void resetVmPowerStateTracking(final long instanceId) {
        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                final VMInstanceVO instance = findById(instanceId);
                if (instance != null) {
                    instance.setPowerStateUpdateCount(0);
                    instance.setPowerStateUpdateTime(DateUtil.currentGMTTime());
                    update(instanceId, instance);
                }
            }
        });
    }

    @Override
    @DB
    public void resetHostPowerStateTracking(final long hostId) {
        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                final SearchCriteria<VMInstanceVO> sc = createSearchCriteria();
                sc.addAnd("powerHostId", SearchCriteria.Op.EQ, hostId);

                final VMInstanceVO instance = createForUpdate();
                instance.setPowerStateUpdateCount(0);
                instance.setPowerStateUpdateTime(DateUtil.currentGMTTime());

                update(instance, sc);
            }
        });
    }

    @Override
    public HashMap<String, Long> countVgpuVMs(final Long dcId, final Long podId, final Long clusterId) {
        final StringBuilder finalQuery = new StringBuilder();
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final List<Long> resourceIdList = new ArrayList<>();
        final HashMap<String, Long> result = new HashMap<>();

        resourceIdList.add(dcId);
        finalQuery.append(COUNT_VMS_BASED_ON_VGPU_TYPES1);

        if (podId != null) {
            finalQuery.append("AND host.pod_id = ? ");
            resourceIdList.add(podId);
        }

        if (clusterId != null) {
            finalQuery.append("AND host.cluster_id = ? ");
            resourceIdList.add(clusterId);
        }
        finalQuery.append(COUNT_VMS_BASED_ON_VGPU_TYPES2);

        try {
            pstmt = txn.prepareAutoCloseStatement(finalQuery.toString());
            for (int i = 0; i < resourceIdList.size(); i++) {
                pstmt.setLong(1 + i, resourceIdList.get(i));
            }
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString(1).concat(rs.getString(2)), rs.getLong(3));
            }
            return result;
        } catch (final SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + finalQuery, e);
        }
    }

    @Override
    public VMInstanceVO findVMByHostNameInZone(final String hostName, final long zoneId) {
        final SearchCriteria<VMInstanceVO> sc = _hostNameAndZoneSearch.create();
        sc.setParameters("hostName", hostName);
        sc.setParameters("zone", zoneId);
        return findOneBy(sc);
    }

    @Override
    public boolean isPowerStateUpToDate(final long instanceId) {
        final VMInstanceVO instance = findById(instanceId);
        if (instance == null) {
            throw new CloudRuntimeException("checking power state update count on non existing instance " + instanceId);
        }
        return instance.getPowerStateUpdateCount() < MAX_CONSECUTIVE_SAME_STATE_UPDATE_COUNT;
    }

    @Override
    public boolean updateState(final State oldState, final Event event, final State newState, final VirtualMachine vm, final Object opaque) {
        if (newState == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("There's no way to transition from old state: " + oldState.toString() + " event: " + event.toString());
            }
            return false;
        }

        final
        Pair<Long, Long> hosts = (Pair<Long, Long>) opaque;
        final Long newHostId = hosts.second();

        final VMInstanceVO vmi = (VMInstanceVO) vm;
        final Long oldHostId = vmi.getHostId();
        final Long oldUpdated = vmi.getUpdated();
        final Date oldUpdateDate = vmi.getUpdateTime();
        if (newState.equals(oldState) && newHostId != null && newHostId.equals(oldHostId)) {
            // state is same, don't need to update
            return true;
        }

        // lock the target row at beginning to avoid lock-promotion caused deadlock
        lockRow(vm.getId(), true);

        final SearchCriteria<VMInstanceVO> sc = _stateChangeSearch.create();
        sc.setParameters("id", vmi.getId());
        sc.setParameters("states", oldState);
        sc.setParameters("host", vmi.getHostId());
        sc.setParameters("update", vmi.getUpdated());

        vmi.incrUpdated();
        final UpdateBuilder ub = getUpdateBuilder(vmi);

        ub.set(vmi, "state", newState);
        ub.set(vmi, "hostId", newHostId);
        ub.set(vmi, "podIdToDeployIn", vmi.getPodIdToDeployIn());
        ub.set(vmi, _updateTimeAttr, new Date());

        final int result = update(vmi, sc);
        if (result == 0) {
            final VMInstanceVO vo = findByIdIncludingRemoved(vm.getId());

            if (s_logger.isDebugEnabled()) {
                if (vo != null) {
                    final StringBuilder str = new StringBuilder("Unable to update ").append(vo.toString());
                    str.append(": DB Data={Host=").append(vo.getHostId()).append("; State=").append(vo.getState().toString()).append("; updated=").append(vo.getUpdated())
                       .append("; time=").append(vo.getUpdateTime());
                    str.append("} New Data: {Host=").append(vm.getHostId()).append("; State=").append(vm.getState().toString()).append("; updated=").append(vmi.getUpdated())
                       .append("; time=").append(vo.getUpdateTime());
                    str.append("} Stale Data: {Host=").append(oldHostId).append("; State=").append(oldState).append("; updated=").append(oldUpdated).append("; time=")
                       .append(oldUpdateDate).append("}");
                    s_logger.debug(str.toString());
                } else {
                    s_logger.debug("Unable to update the vm id=" + vm.getId() + "; the vm either doesn't exist or already removed");
                }
            }

            if (vo != null && vo.getState() == newState) {
                // allow for concurrent update if target state has already been matched
                s_logger.debug("VM " + vo.getInstanceName() + " state has been already been updated to " + newState);
                return true;
            }
        }
        return result > 0;
    }

    @Override
    @DB
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final VMInstanceVO vm = findById(id);
        if (vm != null && vm.getType() == Type.User) {
            _tagsDao.removeByIdAndType(id, ResourceObjectType.UserVm);
        }
        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }
}
