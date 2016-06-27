package org.apache.cloudstack.engine.cloud.entity.api.db.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMReservationVO;

public interface VMReservationDao extends GenericDao<VMReservationVO, Long> {

    VMReservationVO findByVmId(long vmId);

    void loadVolumeReservation(VMReservationVO reservation);

    VMReservationVO findByReservationId(String reservationId);
}
