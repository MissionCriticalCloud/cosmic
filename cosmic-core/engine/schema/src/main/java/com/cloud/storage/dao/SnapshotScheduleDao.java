package com.cloud.storage.dao;

import com.cloud.storage.SnapshotScheduleVO;
import com.cloud.utils.db.GenericDao;

import java.util.Date;
import java.util.List;

/*
 * Data Access Object for snapshot_schedule table
 */
public interface SnapshotScheduleDao extends GenericDao<SnapshotScheduleVO, Long> {

    List<SnapshotScheduleVO> getCoincidingSnapshotSchedules(long volumeId, Date date);

    List<SnapshotScheduleVO> getSchedulesToExecute(Date currentTimestamp);

    SnapshotScheduleVO getCurrentSchedule(Long volumeId, Long policyId, boolean executing);

    SnapshotScheduleVO findOneByVolume(long volumeId);

    SnapshotScheduleVO findOneByVolumePolicy(long volumeId, long policyId);
}
