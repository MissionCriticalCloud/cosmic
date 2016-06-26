package com.cloud.storage.dao;

import com.cloud.storage.StoragePoolWorkVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB()
public class StoragePoolWorkDaoImpl extends GenericDaoBase<StoragePoolWorkVO, Long> implements StoragePoolWorkDao {

    protected final SearchBuilder<StoragePoolWorkVO> PendingWorkForPrepareForMaintenanceSearch;
    protected final SearchBuilder<StoragePoolWorkVO> PendingWorkForCancelMaintenanceSearch;
    protected final SearchBuilder<StoragePoolWorkVO> PoolAndVmIdSearch;
    protected final SearchBuilder<StoragePoolWorkVO> PendingJobsForDeadMs;

    private final String FindPoolIds = "SELECT distinct storage_pool_work.pool_id FROM storage_pool_work WHERE mgmt_server_id = ?";

    protected StoragePoolWorkDaoImpl() {
        PendingWorkForPrepareForMaintenanceSearch = createSearchBuilder();
        PendingWorkForPrepareForMaintenanceSearch.and("poolId", PendingWorkForPrepareForMaintenanceSearch.entity().getPoolId(), SearchCriteria.Op.EQ);
        PendingWorkForPrepareForMaintenanceSearch.and("stoppedForMaintenance", PendingWorkForPrepareForMaintenanceSearch.entity().isStoppedForMaintenance(),
                SearchCriteria.Op.EQ);
        PendingWorkForPrepareForMaintenanceSearch.and("startedAfterMaintenance", PendingWorkForPrepareForMaintenanceSearch.entity().isStartedAfterMaintenance(),
                SearchCriteria.Op.EQ);
        PendingWorkForPrepareForMaintenanceSearch.done();

        PendingWorkForCancelMaintenanceSearch = createSearchBuilder();
        PendingWorkForCancelMaintenanceSearch.and("poolId", PendingWorkForCancelMaintenanceSearch.entity().getPoolId(), SearchCriteria.Op.EQ);
        PendingWorkForCancelMaintenanceSearch.and("stoppedForMaintenance", PendingWorkForCancelMaintenanceSearch.entity().isStoppedForMaintenance(), SearchCriteria.Op.EQ);
        PendingWorkForCancelMaintenanceSearch.and("startedAfterMaintenance", PendingWorkForCancelMaintenanceSearch.entity().isStartedAfterMaintenance(),
                SearchCriteria.Op.EQ);
        PendingWorkForCancelMaintenanceSearch.done();

        PoolAndVmIdSearch = createSearchBuilder();
        PoolAndVmIdSearch.and("poolId", PoolAndVmIdSearch.entity().getPoolId(), SearchCriteria.Op.EQ);
        PoolAndVmIdSearch.and("vmId", PoolAndVmIdSearch.entity().getVmId(), SearchCriteria.Op.EQ);
        PoolAndVmIdSearch.done();

        PendingJobsForDeadMs = createSearchBuilder();
        PendingJobsForDeadMs.and("managementServerId", PendingJobsForDeadMs.entity().getManagementServerId(), SearchCriteria.Op.EQ);
        PendingJobsForDeadMs.and("poolId", PendingJobsForDeadMs.entity().getPoolId(), SearchCriteria.Op.EQ);
        PendingJobsForDeadMs.and("stoppedForMaintenance", PendingJobsForDeadMs.entity().isStoppedForMaintenance(), SearchCriteria.Op.EQ);
        PendingJobsForDeadMs.and("startedAfterMaintenance", PendingJobsForDeadMs.entity().isStartedAfterMaintenance(), SearchCriteria.Op.EQ);
        PendingJobsForDeadMs.done();
    }

    @Override
    public List<StoragePoolWorkVO> listPendingWorkForPrepareForMaintenanceByPoolId(final long poolId) {
        final SearchCriteria<StoragePoolWorkVO> sc = PendingWorkForPrepareForMaintenanceSearch.create();
        sc.setParameters("poolId", poolId);
        sc.setParameters("stoppedForMaintenance", false);
        sc.setParameters("startedAfterMaintenance", false);
        return listBy(sc);
    }

    @Override
    public List<StoragePoolWorkVO> listPendingWorkForCancelMaintenanceByPoolId(final long poolId) {
        final SearchCriteria<StoragePoolWorkVO> sc = PendingWorkForCancelMaintenanceSearch.create();
        sc.setParameters("poolId", poolId);
        sc.setParameters("stoppedForMaintenance", true);
        sc.setParameters("startedAfterMaintenance", false);
        return listBy(sc);
    }

    @Override
    public StoragePoolWorkVO findByPoolIdAndVmId(final long poolId, final long vmId) {
        final SearchCriteria<StoragePoolWorkVO> sc = PoolAndVmIdSearch.create();
        sc.setParameters("poolId", poolId);
        sc.setParameters("vmId", vmId);
        return listBy(sc).get(0);
    }

    @Override
    public void removePendingJobsOnMsRestart(final long msId, final long poolId) {
        // hung jobs are those which are stopped, but never started
        final SearchCriteria<StoragePoolWorkVO> sc = PendingJobsForDeadMs.create();
        sc.setParameters("managementServerId", msId);
        sc.setParameters("poolId", poolId);
        sc.setParameters("stoppedForMaintenance", true);
        sc.setParameters("startedAfterMaintenance", false);
        remove(sc);
    }

    @Override
    @DB
    public List<Long> searchForPoolIdsForPendingWorkJobs(final long msId) {
        final StringBuilder sql = new StringBuilder(FindPoolIds);
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        final List<Long> poolIds = new ArrayList<>();
        try (PreparedStatement pstmt = txn.prepareStatement(sql.toString())) {
            if (pstmt != null) {
                pstmt.setLong(1, msId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        poolIds.add(rs.getLong("pool_id"));
                    }
                } catch (final SQLException e) {
                    throw new CloudRuntimeException("searchForPoolIdsForPendingWorkJobs:Exception:" + e.getMessage(), e);
                }
            }
            return poolIds;
        } catch (final SQLException e) {
            throw new CloudRuntimeException("searchForPoolIdsForPendingWorkJobs:Exception:" + e.getMessage(), e);
        }
    }
}
