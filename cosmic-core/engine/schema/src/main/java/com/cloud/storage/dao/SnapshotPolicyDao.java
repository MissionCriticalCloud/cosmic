package com.cloud.storage.dao;

import com.cloud.storage.SnapshotPolicyVO;
import com.cloud.utils.DateUtil.IntervalType;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;

import java.util.List;

/*
 * Data Access Object for snapshot_policy table
 */
public interface SnapshotPolicyDao extends GenericDao<SnapshotPolicyVO, Long> {
    List<SnapshotPolicyVO> listByVolumeId(long volumeId);

    List<SnapshotPolicyVO> listByVolumeId(long volumeId, Filter filter);

    Pair<List<SnapshotPolicyVO>, Integer> listAndCountByVolumeId(long volumeId, boolean display);

    Pair<List<SnapshotPolicyVO>, Integer> listAndCountByVolumeId(long volumeId, boolean display, Filter filter);

    SnapshotPolicyVO findOneByVolumeInterval(long volumeId, IntervalType intvType);

    List<SnapshotPolicyVO> listActivePolicies();

    SnapshotPolicyVO findOneByVolume(long volumeId);

    Pair<List<SnapshotPolicyVO>, Integer> listAndCountById(long id, boolean display, Filter filter);
}
