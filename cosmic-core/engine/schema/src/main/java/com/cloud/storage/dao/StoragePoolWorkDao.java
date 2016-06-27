package com.cloud.storage.dao;

import com.cloud.storage.StoragePoolWorkVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

/**
 * Data Access Object for storage_pool table
 */
public interface StoragePoolWorkDao extends GenericDao<StoragePoolWorkVO, Long> {

    List<StoragePoolWorkVO> listPendingWorkForPrepareForMaintenanceByPoolId(long poolId);

    List<StoragePoolWorkVO> listPendingWorkForCancelMaintenanceByPoolId(long poolId);

    StoragePoolWorkVO findByPoolIdAndVmId(long poolId, long vmId);

    void removePendingJobsOnMsRestart(long msId, long poolId);

    List<Long> searchForPoolIdsForPendingWorkJobs(long msId);
}
