package com.cloud.engine.cloud.entity.api.db.dao;

import com.cloud.engine.cloud.entity.api.db.VolumeReservationVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface VolumeReservationDao extends GenericDao<VolumeReservationVO, Long> {

    VolumeReservationVO findByVmId(long vmId);

    List<VolumeReservationVO> listVolumeReservation(long vmReservationId);
}
