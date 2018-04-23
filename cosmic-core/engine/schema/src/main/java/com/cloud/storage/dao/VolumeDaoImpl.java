package com.cloud.storage.dao;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.storage.ScopeType;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Volume;
import com.cloud.storage.Volume.Event;
import com.cloud.storage.Volume.State;
import com.cloud.storage.Volume.Type;
import com.cloud.storage.VolumeVO;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.utils.Pair;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.InvalidParameterValueException;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VolumeDaoImpl extends GenericDaoBase<VolumeVO, Long> implements VolumeDao {
    protected static final String SELECT_VM_SQL = "SELECT DISTINCT instance_id from volumes v where v.host_id = ? and v.mirror_state = ?";
    // need to account for zone-wide primary storage where storage_pool has
    // null-value pod and cluster, where hypervisor information is stored in
    // storage_pool
    protected static final String SELECT_HYPERTYPE_FROM_CLUSTER_VOLUME =
            "SELECT c.hypervisor_type from volumes v, storage_pool s, cluster c where v.pool_id = s.id and s.cluster_id = c.id and v.id = ?";
    protected static final String SELECT_HYPERTYPE_FROM_ZONE_VOLUME = "SELECT s.hypervisor from volumes v, storage_pool s where v.pool_id = s.id and v.id = ?";
    protected static final String SELECT_POOLSCOPE = "SELECT s.scope from storage_pool s, volumes v where s.id = v.pool_id and v.id = ?";
    private static final Logger s_logger = LoggerFactory.getLogger(VolumeDaoImpl.class);
    private static final String ORDER_POOLS_NUMBER_OF_VOLUMES_FOR_ACCOUNT =
            "SELECT pool.id, SUM(IF(vol.state='Ready' AND vol.account_id = ?, 1, 0)) FROM `cloud`.`storage_pool` pool LEFT JOIN `cloud`.`volumes` vol ON pool.id = vol.pool_id " +
                    "WHERE pool.data_center_id = ? "
                    + " AND pool.pod_id = ? AND pool.cluster_id = ? " + " GROUP BY pool.id ORDER BY 2 ASC ";
    private static final String ORDER_ZONE_WIDE_POOLS_NUMBER_OF_VOLUMES_FOR_ACCOUNT =
            "SELECT pool.id, SUM(IF(vol.state='Ready' AND vol.account_id = ?, 1, 0)) FROM `cloud`.`storage_pool` pool LEFT JOIN `cloud`.`volumes` vol ON pool.id = vol.pool_id " +
                    "WHERE pool.data_center_id = ? "
                    + " AND pool.scope = 'ZONE' AND pool.status='Up' " + " GROUP BY pool.id ORDER BY 2 ASC ";
    protected final SearchBuilder<VolumeVO> DetachedAccountIdSearch;
    protected final SearchBuilder<VolumeVO> TemplateZoneSearch;
    protected final GenericSearchBuilder<VolumeVO, SumCount> TotalSizeByPoolSearch;
    protected final GenericSearchBuilder<VolumeVO, SumCount> TotalVMSnapshotSizeByPoolSearch;
    protected final GenericSearchBuilder<VolumeVO, Long> ActiveTemplateSearch;
    protected final SearchBuilder<VolumeVO> InstanceStatesSearch;
    protected final SearchBuilder<VolumeVO> AllFieldsSearch;
    protected GenericSearchBuilder<VolumeVO, Long> CountByAccount;
    protected GenericSearchBuilder<VolumeVO, SumCount> primaryStorageSearch;
    protected GenericSearchBuilder<VolumeVO, SumCount> primaryStorageSearch2;
    protected GenericSearchBuilder<VolumeVO, SumCount> secondaryStorageSearch;
    @Inject
    ResourceTagDao _tagsDao;

    public VolumeDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("accountId", AllFieldsSearch.entity().getAccountId(), Op.EQ);
        AllFieldsSearch.and("dcId", AllFieldsSearch.entity().getDataCenterId(), Op.EQ);
        AllFieldsSearch.and("pod", AllFieldsSearch.entity().getPodId(), Op.EQ);
        AllFieldsSearch.and("instanceId", AllFieldsSearch.entity().getInstanceId(), Op.EQ);
        AllFieldsSearch.and("deviceId", AllFieldsSearch.entity().getDeviceId(), Op.EQ);
        AllFieldsSearch.and("poolId", AllFieldsSearch.entity().getPoolId(), Op.EQ);
        AllFieldsSearch.and("vType", AllFieldsSearch.entity().getVolumeType(), Op.EQ);
        AllFieldsSearch.and("notVolumeType", AllFieldsSearch.entity().getVolumeType(), Op.NEQ);
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("destroyed", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("notDestroyed", AllFieldsSearch.entity().getState(), Op.NEQ);
        AllFieldsSearch.and("updateTime", AllFieldsSearch.entity().getUpdated(), SearchCriteria.Op.LT);
        AllFieldsSearch.and("updatedCount", AllFieldsSearch.entity().getUpdatedCount(), Op.EQ);
        AllFieldsSearch.and("name", AllFieldsSearch.entity().getName(), Op.EQ);
        AllFieldsSearch.done();

        DetachedAccountIdSearch = createSearchBuilder();
        DetachedAccountIdSearch.and("accountId", DetachedAccountIdSearch.entity().getAccountId(), Op.EQ);
        DetachedAccountIdSearch.and("destroyed", DetachedAccountIdSearch.entity().getState(), Op.NEQ);
        DetachedAccountIdSearch.and("instanceId", DetachedAccountIdSearch.entity().getInstanceId(), Op.NULL);
        DetachedAccountIdSearch.done();

        TemplateZoneSearch = createSearchBuilder();
        TemplateZoneSearch.and("template", TemplateZoneSearch.entity().getTemplateId(), Op.EQ);
        TemplateZoneSearch.and("zone", TemplateZoneSearch.entity().getDataCenterId(), Op.EQ);
        TemplateZoneSearch.done();

        TotalSizeByPoolSearch = createSearchBuilder(SumCount.class);
        TotalSizeByPoolSearch.select("sum", Func.SUM, TotalSizeByPoolSearch.entity().getSize());
        TotalSizeByPoolSearch.select("count", Func.COUNT, (Object[]) null);
        TotalSizeByPoolSearch.and("poolId", TotalSizeByPoolSearch.entity().getPoolId(), Op.EQ);
        TotalSizeByPoolSearch.and("removed", TotalSizeByPoolSearch.entity().getRemoved(), Op.NULL);
        TotalSizeByPoolSearch.and("state", TotalSizeByPoolSearch.entity().getState(), Op.NEQ);
        TotalSizeByPoolSearch.done();

        TotalVMSnapshotSizeByPoolSearch = createSearchBuilder(SumCount.class);
        TotalVMSnapshotSizeByPoolSearch.select("sum", Func.SUM, TotalVMSnapshotSizeByPoolSearch.entity().getVmSnapshotChainSize());
        TotalVMSnapshotSizeByPoolSearch.and("poolId", TotalVMSnapshotSizeByPoolSearch.entity().getPoolId(), Op.EQ);
        TotalVMSnapshotSizeByPoolSearch.and("removed", TotalVMSnapshotSizeByPoolSearch.entity().getRemoved(), Op.NULL);
        TotalVMSnapshotSizeByPoolSearch.and("state", TotalVMSnapshotSizeByPoolSearch.entity().getState(), Op.NEQ);
        TotalVMSnapshotSizeByPoolSearch.and("vType", TotalVMSnapshotSizeByPoolSearch.entity().getVolumeType(), Op.EQ);
        TotalVMSnapshotSizeByPoolSearch.and("instanceId", TotalVMSnapshotSizeByPoolSearch.entity().getInstanceId(), Op.NNULL);
        TotalVMSnapshotSizeByPoolSearch.done();

        ActiveTemplateSearch = createSearchBuilder(Long.class);
        ActiveTemplateSearch.and("pool", ActiveTemplateSearch.entity().getPoolId(), Op.EQ);
        ActiveTemplateSearch.and("template", ActiveTemplateSearch.entity().getTemplateId(), Op.EQ);
        ActiveTemplateSearch.and("removed", ActiveTemplateSearch.entity().getRemoved(), Op.NULL);
        ActiveTemplateSearch.select(null, Func.COUNT, null);
        ActiveTemplateSearch.done();

        InstanceStatesSearch = createSearchBuilder();
        InstanceStatesSearch.and("instance", InstanceStatesSearch.entity().getInstanceId(), Op.EQ);
        InstanceStatesSearch.and("states", InstanceStatesSearch.entity().getState(), Op.IN);
        InstanceStatesSearch.done();

        CountByAccount = createSearchBuilder(Long.class);
        CountByAccount.select(null, Func.COUNT, null);
        CountByAccount.and("account", CountByAccount.entity().getAccountId(), SearchCriteria.Op.EQ);
        CountByAccount.and("state", CountByAccount.entity().getState(), SearchCriteria.Op.NIN);
        CountByAccount.and("displayVolume", CountByAccount.entity().isDisplayVolume(), Op.EQ);
        CountByAccount.done();

        primaryStorageSearch = createSearchBuilder(SumCount.class);
        primaryStorageSearch.select("sum", Func.SUM, primaryStorageSearch.entity().getSize());
        primaryStorageSearch.and("accountId", primaryStorageSearch.entity().getAccountId(), Op.EQ);
        primaryStorageSearch.and().op("path", primaryStorageSearch.entity().getPath(), Op.NNULL);
        primaryStorageSearch.or("states", primaryStorageSearch.entity().getState(), Op.IN);
        primaryStorageSearch.cp();
        primaryStorageSearch.and("displayVolume", primaryStorageSearch.entity().isDisplayVolume(), Op.EQ);
        primaryStorageSearch.and("isRemoved", primaryStorageSearch.entity().getRemoved(), Op.NULL);
        primaryStorageSearch.done();

        primaryStorageSearch2 = createSearchBuilder(SumCount.class);
        primaryStorageSearch2.select("sum", Func.SUM, primaryStorageSearch2.entity().getSize());
        primaryStorageSearch2.and("accountId", primaryStorageSearch2.entity().getAccountId(), Op.EQ);
        primaryStorageSearch2.and().op("instanceId", primaryStorageSearch2.entity().getInstanceId(), Op.NULL);
        primaryStorageSearch2.or("virtualRouterVmIds", primaryStorageSearch2.entity().getInstanceId(), Op.NIN);
        primaryStorageSearch2.cp();
        primaryStorageSearch2.and().op("path", primaryStorageSearch2.entity().getPath(), Op.NNULL);
        primaryStorageSearch2.or("states", primaryStorageSearch2.entity().getState(), Op.IN);
        primaryStorageSearch2.cp();
        primaryStorageSearch2.and("displayVolume", primaryStorageSearch2.entity().isDisplayVolume(), Op.EQ);
        primaryStorageSearch2.and("isRemoved", primaryStorageSearch2.entity().getRemoved(), Op.NULL);
        primaryStorageSearch2.done();

        secondaryStorageSearch = createSearchBuilder(SumCount.class);
        secondaryStorageSearch.select("sum", Func.SUM, secondaryStorageSearch.entity().getSize());
        secondaryStorageSearch.and("accountId", secondaryStorageSearch.entity().getAccountId(), Op.EQ);
        secondaryStorageSearch.and("path", secondaryStorageSearch.entity().getPath(), Op.NULL);
        secondaryStorageSearch.and("states", secondaryStorageSearch.entity().getState(), Op.NIN);
        secondaryStorageSearch.and("isRemoved", secondaryStorageSearch.entity().getRemoved(), Op.NULL);
        secondaryStorageSearch.done();
    }

    @Override
    public List<VolumeVO> findDetachedByAccount(final long accountId) {
        final SearchCriteria<VolumeVO> sc = DetachedAccountIdSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("destroyed", Volume.State.Destroy);
        return listBy(sc);
    }

    @Override
    public List<VolumeVO> findByAccount(final long accountId) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }

    @Override
    public List<VolumeVO> findIncludingRemovedByAccount(long accountId) {
        SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    @DB()
    public Pair<Long, Long> getCountAndTotalByPool(final long poolId) {
        final SearchCriteria<SumCount> sc = TotalSizeByPoolSearch.create();
        sc.setParameters("poolId", poolId);
        final List<SumCount> results = customSearch(sc, null);
        final SumCount sumCount = results.get(0);
        return new Pair<>(sumCount.count, sumCount.sum);
    }

    @Override
    @DB()
    public Pair<Long, Long> getNonDestroyedCountAndTotalByPool(final long poolId) {
        final SearchCriteria<SumCount> sc = TotalSizeByPoolSearch.create();
        sc.setParameters("poolId", poolId);
        sc.setParameters("state", State.Destroy);
        final List<SumCount> results = customSearch(sc, null);
        final SumCount sumCount = results.get(0);
        return new Pair<>(sumCount.count, sumCount.sum);
    }

    @Override
    public long getVMSnapshotSizeByPool(final long poolId) {
        final SearchCriteria<SumCount> sc = TotalVMSnapshotSizeByPoolSearch.create();
        sc.setParameters("poolId", poolId);
        sc.setParameters("state", State.Destroy);
        sc.setParameters("vType", Volume.Type.ROOT.toString());
        final List<SumCount> results = customSearch(sc, null);
        if (results != null) {
            return results.get(0).sum;
        } else {
            return 0;
        }
    }

    @Override
    public List<VolumeVO> findByInstance(final long id) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", id);
        return listBy(sc);
    }

    @Override
    public List<VolumeVO> findByInstanceAndType(final long id, final Type vType) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", id);
        sc.setParameters("vType", vType.toString());
        return listBy(sc);
    }

    @Override
    public List<VolumeVO> findByInstanceIdDestroyed(final long vmId) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", vmId);
        sc.setParameters("destroyed", Volume.State.Destroy);
        return listBy(sc);
    }

    @Override
    public List<VolumeVO> findByPod(final long podId) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("pod", podId);

        return listBy(sc);
    }

    @Override
    public List<VolumeVO> findByDc(final long dcId) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("dcId", dcId);

        return listBy(sc);
    }

    @Override
    public List<VolumeVO> findByAccountAndPod(final long accountId, final long podId) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("pod", podId);
        sc.setParameters("state", Volume.State.Ready);

        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<VolumeVO> findByTemplateAndZone(final long templateId, final long zoneId) {
        final SearchCriteria<VolumeVO> sc = TemplateZoneSearch.create();
        sc.setParameters("template", templateId);
        sc.setParameters("zone", zoneId);

        return listIncludingRemovedBy(sc);
    }

    @Override
    public void deleteVolumesByInstance(final long instanceId) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", instanceId);
        expunge(sc);
    }

    @Override
    public void attachVolume(final long volumeId, final long vmId, final long deviceId, final DiskControllerType diskController) {
        final VolumeVO volume = createForUpdate(volumeId);
        volume.setInstanceId(vmId);
        volume.setDeviceId(deviceId);
        volume.setUpdated(new Date());
        volume.setAttached(new Date());
        if (deviceId == 0L) {
            volume.setVolumeType(Type.ROOT);
        }
        if (diskController != null) {
            volume.setDiskController(diskController);
        }
        update(volumeId, volume);
    }

    @Override
    public void detachVolume(final long volumeId) {
        final VolumeVO volume = createForUpdate(volumeId);
        volume.setInstanceId(null);
        volume.setDeviceId(null);
        volume.setUpdated(new Date());
        volume.setAttached(null);
        if (findById(volumeId).getVolumeType() == Type.ROOT) {
            volume.setVolumeType(Type.DATADISK);
        }
        update(volumeId, volume);
    }

    @Override
    public boolean isAnyVolumeActivelyUsingTemplateOnPool(final long templateId, final long poolId) {
        final SearchCriteria<Long> sc = ActiveTemplateSearch.create();
        sc.setParameters("template", templateId);
        sc.setParameters("pool", poolId);

        final List<Long> results = customSearchIncludingRemoved(sc, null);
        assert results.size() > 0 : "How can this return a size of " + results.size();

        return results.get(0) > 0;
    }

    @Override
    public List<VolumeVO> findCreatedByInstance(final long id) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", id);
        sc.setParameters("state", Volume.State.Ready);
        return listBy(sc);
    }

    @Override
    public List<VolumeVO> findByPoolId(final long poolId) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("poolId", poolId);
        sc.setParameters("notDestroyed", Volume.State.Destroy);
        sc.setParameters("vType", Volume.Type.ROOT.toString());
        return listBy(sc);
    }

    @Override
    public VolumeVO findByPoolIdName(final long poolId, final String name) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("poolId", poolId);
        sc.setParameters("name", name);
        return findOneBy(sc);
    }

    @Override
    public List<VolumeVO> findByPoolId(final long poolId, final Volume.Type volumeType) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("poolId", poolId);
        sc.setParameters("notDestroyed", Volume.State.Destroy);

        if (volumeType != null) {
            sc.setParameters("vType", volumeType.toString());
        }

        return listBy(sc);
    }

    @Override
    public List<VolumeVO> findByInstanceAndDeviceId(final long instanceId, final long deviceId) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", instanceId);
        sc.setParameters("deviceId", deviceId);
        return listBy(sc);
    }

    @Override
    public List<VolumeVO> findUsableVolumesForInstance(final long instanceId) {
        final SearchCriteria<VolumeVO> sc = InstanceStatesSearch.create();
        sc.setParameters("instance", instanceId);
        sc.setParameters("states", Volume.State.Creating, Volume.State.Ready, Volume.State.Allocated);

        return listBy(sc);
    }

    @Override
    public Long countAllocatedVolumesForAccount(final long accountId) {
        final SearchCriteria<Long> sc = CountByAccount.create();
        sc.setParameters("account", accountId);
        sc.setParameters("state", Volume.State.Destroy);
        sc.setParameters("displayVolume", 1);
        return customSearch(sc, null).get(0);
    }

    @Override
    @DB
    public HypervisorType getHypervisorType(final long volumeId) {
        /* lookup from cluster of pool */
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        String sql = null;
        try {
            final ScopeType scope = getVolumeStoragePoolScope(volumeId);
            if (scope != null) {
                if (scope == ScopeType.CLUSTER || scope == ScopeType.HOST) {
                    sql = SELECT_HYPERTYPE_FROM_CLUSTER_VOLUME;
                } else if (scope == ScopeType.ZONE) {
                    sql = SELECT_HYPERTYPE_FROM_ZONE_VOLUME;
                } else {
                    s_logger.error("Unhandled scope type '" + scope + "' when running getHypervisorType on volume id " + volumeId);
                }

                pstmt = txn.prepareAutoCloseStatement(sql);
                pstmt.setLong(1, volumeId);
                final ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    if (rs.getString(1) != null) {
                        return HypervisorType.getType(rs.getString(1));
                    }
                }
            }
            return HypervisorType.None;
        } catch (final SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + sql, e);
        }
    }

    @Override
    public List<VolumeVO> listVolumesToBeDestroyed() {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("state", Volume.State.Destroy);

        return listBy(sc);
    }

    @Override
    public List<VolumeVO> listNonRootVolumesToBeDestroyed(final Date date) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("state", Volume.State.Destroy);
        sc.setParameters("notVolumeType", Volume.Type.ROOT.toString());
        sc.setParameters("updateTime", date);

        return listBy(sc);
    }

    @Override
    public ImageFormat getImageFormat(final Long volumeId) {
        final HypervisorType type = getHypervisorType(volumeId);
        if (type.equals(HypervisorType.KVM)) {
            return ImageFormat.QCOW2;
        } else if (type.equals(HypervisorType.XenServer)) {
            return ImageFormat.VHD;
        } else {
            s_logger.warn("Do not support hypervisor " + type.toString());
            return null;
        }
    }

    @Override
    public List<VolumeVO> findReadyRootVolumesByInstance(final long instanceId) {
        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("instanceId", instanceId);
        sc.setParameters("state", Volume.State.Ready);
        sc.setParameters("vType", Volume.Type.ROOT);
        return listBy(sc);
    }

    @Override
    public List<Long> listPoolIdsByVolumeCount(final long dcId, final Long podId, final Long clusterId, final long accountId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final List<Long> result = new ArrayList<>();
        try {
            final String sql = ORDER_POOLS_NUMBER_OF_VOLUMES_FOR_ACCOUNT;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, accountId);
            pstmt.setLong(2, dcId);
            pstmt.setLong(3, podId);
            pstmt.setLong(4, clusterId);

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
            return result;
        } catch (final SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + ORDER_POOLS_NUMBER_OF_VOLUMES_FOR_ACCOUNT, e);
        }
    }

    @Override
    public List<Long> listZoneWidePoolIdsByVolumeCount(final long dcId, final long accountId) {

        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final List<Long> result = new ArrayList<>();
        try {
            final String sql = ORDER_ZONE_WIDE_POOLS_NUMBER_OF_VOLUMES_FOR_ACCOUNT;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, accountId);
            pstmt.setLong(2, dcId);

            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
            return result;
        } catch (final SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + ORDER_ZONE_WIDE_POOLS_NUMBER_OF_VOLUMES_FOR_ACCOUNT, e);
        }
    }

    @Override
    public long primaryStorageUsedForAccount(final long accountId, final List<Long> virtualRouters) {
        final SearchCriteria<SumCount> sc;
        if (!virtualRouters.isEmpty()) {
            sc = primaryStorageSearch2.create();
            sc.setParameters("virtualRouterVmIds", virtualRouters.toArray(new Object[virtualRouters.size()]));
        } else {
            sc = primaryStorageSearch.create();
        }
        sc.setParameters("accountId", accountId);
        sc.setParameters("states", State.Allocated);
        sc.setParameters("displayVolume", 1);
        final List<SumCount> storageSpace = customSearch(sc, null);
        if (storageSpace != null) {
            return storageSpace.get(0).sum;
        } else {
            return 0;
        }
    }

    @Override
    public long secondaryStorageUsedForAccount(final long accountId) {
        final SearchCriteria<SumCount> sc = secondaryStorageSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("states", State.Allocated);
        final List<SumCount> storageSpace = customSearch(sc, null);
        if (storageSpace != null) {
            return storageSpace.get(0).sum;
        } else {
            return 0;
        }
    }

    @Override
    public ScopeType getVolumeStoragePoolScope(final long volumeId) {
        // finding the storage scope where the volume is present
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;

        try {
            final String sql = SELECT_POOLSCOPE;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, volumeId);
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                final String scope = rs.getString(1);
                if (scope != null) {
                    try {
                        return Enum.valueOf(ScopeType.class, scope.toUpperCase());
                    } catch (final Exception e) {
                        throw new InvalidParameterValueException("invalid scope for pool " + scope);
                    }
                }
            }
        } catch (final SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + SELECT_POOLSCOPE, e);
        }
        return null;
    }

    @Override
    @DB
    public boolean updateUuid(final long srcVolId, final long destVolId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        try {
            final VolumeVO srcVol = findById(srcVolId);
            final VolumeVO destVol = findById(destVolId);
            final String uuid = srcVol.getUuid();
            final Long instanceId = srcVol.getInstanceId();
            srcVol.setUuid(null);
            srcVol.setInstanceId(null);
            destVol.setUuid(uuid);
            destVol.setInstanceId(instanceId);
            update(srcVolId, srcVol);
            update(destVolId, destVol);
            _tagsDao.updateResourceId(srcVolId, destVolId, ResourceObjectType.Volume);
        } catch (final Exception e) {
            throw new CloudRuntimeException("Unable to persist the sequence number for this host");
        }
        txn.commit();
        return true;
    }

    @Override
    public boolean updateState(final com.cloud.storage.Volume.State currentState, final Event event, final com.cloud.storage.Volume.State nextState, final Volume vo, final
    Object data) {

        final Long oldUpdated = vo.getUpdatedCount();
        final Date oldUpdatedTime = vo.getUpdated();

        final SearchCriteria<VolumeVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", vo.getId());
        sc.setParameters("state", currentState);
        sc.setParameters("updatedCount", vo.getUpdatedCount());

        vo.incrUpdatedCount();

        final UpdateBuilder builder = getUpdateBuilder(vo);
        builder.set(vo, "state", nextState);
        builder.set(vo, "updated", new Date());

        final int rows = update((VolumeVO) vo, sc);
        if (rows == 0 && s_logger.isDebugEnabled()) {
            final VolumeVO dbVol = findByIdIncludingRemoved(vo.getId());
            if (dbVol != null) {
                final StringBuilder str = new StringBuilder("Unable to update ").append(vo.toString());
                str.append(": DB Data={id=")
                   .append(dbVol.getId())
                   .append("; state=")
                   .append(dbVol.getState())
                   .append("; updatecount=")
                   .append(dbVol.getUpdatedCount())
                   .append(";updatedTime=")
                   .append(dbVol.getUpdated());
                str.append(": New Data={id=")
                   .append(vo.getId())
                   .append("; state=")
                   .append(nextState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(vo.getUpdatedCount())
                   .append("; updatedTime=")
                   .append(vo.getUpdated());
                str.append(": stale Data={id=")
                   .append(vo.getId())
                   .append("; state=")
                   .append(currentState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(oldUpdated)
                   .append("; updatedTime=")
                   .append(oldUpdatedTime);
            } else {
                s_logger.debug("Unable to update volume: id=" + vo.getId() + ", as there is no such volume exists in the database anymore");
            }
        }
        return rows > 0;
    }

    @Override
    @DB
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final VolumeVO entry = findById(id);
        if (entry != null) {
            _tagsDao.removeByIdAndType(id, ResourceObjectType.Volume);
        }
        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }

    public static class SumCount {
        public long sum;
        public long count;

        public SumCount() {
        }
    }
}
