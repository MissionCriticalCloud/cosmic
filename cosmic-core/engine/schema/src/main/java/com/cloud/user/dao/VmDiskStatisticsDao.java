package com.cloud.user.dao;

import com.cloud.user.VmDiskStatisticsVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

public interface VmDiskStatisticsDao extends GenericDao<VmDiskStatisticsVO, Long> {
    VmDiskStatisticsVO findBy(long accountId, long dcId, long vmId, long volumeId);

    VmDiskStatisticsVO lock(long accountId, long dcId, long vmId, long volumeId);

    List<VmDiskStatisticsVO> listBy(long accountId);

    List<VmDiskStatisticsVO> listActiveAndRecentlyDeleted(Date minRemovedDate, int startIndex, int limit);

    List<VmDiskStatisticsVO> listUpdatedStats();
}
