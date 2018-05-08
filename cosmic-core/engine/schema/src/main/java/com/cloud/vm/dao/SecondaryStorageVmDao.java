package com.cloud.vm.dao;

import com.cloud.legacymodel.storage.SecondaryStorageVmRole;
import com.cloud.legacymodel.vm.VirtualMachine.State;
import com.cloud.utils.db.GenericDao;
import com.cloud.vm.SecondaryStorageVmVO;

import java.util.List;

public interface SecondaryStorageVmDao extends GenericDao<SecondaryStorageVmVO, Long> {

    List<SecondaryStorageVmVO> getSecStorageVmListInStates(SecondaryStorageVmRole role, long dataCenterId, State... states);

    List<SecondaryStorageVmVO> getSecStorageVmListInStates(SecondaryStorageVmRole role, State... states);

    List<SecondaryStorageVmVO> listByHostId(SecondaryStorageVmRole role, long hostId);

    List<SecondaryStorageVmVO> listByLastHostId(SecondaryStorageVmRole role, long hostId);

    List<SecondaryStorageVmVO> listUpByHostId(SecondaryStorageVmRole role, long hostId);

    List<SecondaryStorageVmVO> listByZoneId(SecondaryStorageVmRole role, long zoneId);

    SecondaryStorageVmVO findByInstanceName(String instanceName);
}
