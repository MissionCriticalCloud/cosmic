package com.cloud.deploy.dao;

import com.cloud.deploy.PlannerHostReservationVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface PlannerHostReservationDao extends GenericDao<PlannerHostReservationVO, Long> {

    PlannerHostReservationVO findByHostId(long hostId);

    List<PlannerHostReservationVO> listAllReservedHosts();

    List<PlannerHostReservationVO> listAllDedicatedHosts();
}
