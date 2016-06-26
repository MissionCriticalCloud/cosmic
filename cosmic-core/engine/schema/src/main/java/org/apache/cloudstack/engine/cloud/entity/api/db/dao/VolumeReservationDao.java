package org.apache.cloudstack.engine.cloud.entity.api.db.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.engine.cloud.entity.api.db.VolumeReservationVO;

import java.util.List;

public interface VolumeReservationDao extends GenericDao<VolumeReservationVO, Long> {

    VolumeReservationVO findByVmId(long vmId);

    List<VolumeReservationVO> listVolumeReservation(long vmReservationId);
}
