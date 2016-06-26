package org.apache.cloudstack.engine.cloud.entity.api.db.dao;

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMEntityVO;

/*
 * Data Access Object for vm_instance table
 */
public interface VMEntityDao extends GenericDao<VMEntityVO, Long> {

    void loadVmReservation(VMEntityVO vm);
}
