package com.cloud.engine.cloud.entity.api.db.dao;

import com.cloud.engine.cloud.entity.api.db.VMEntityVO;
import com.cloud.utils.db.GenericDao;

/*
 * Data Access Object for vm_instance table
 */
public interface VMEntityDao extends GenericDao<VMEntityVO, Long> {

    void loadVmReservation(VMEntityVO vm);
}
