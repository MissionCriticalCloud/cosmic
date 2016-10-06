package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.vm.SecondaryStorageVm;
import com.cloud.vm.SecondaryStorageVmVO;
import com.cloud.vm.VirtualMachine.State;

import java.util.List;

public interface SecondaryStorageVmDao extends GenericDao<SecondaryStorageVmVO, Long> {

    List<SecondaryStorageVmVO> getSecStorageVmListInStates(SecondaryStorageVm.Role role, long dataCenterId, State... states);

    List<SecondaryStorageVmVO> getSecStorageVmListInStates(SecondaryStorageVm.Role role, State... states);

    List<SecondaryStorageVmVO> listByHostId(SecondaryStorageVm.Role role, long hostId);

    List<SecondaryStorageVmVO> listByLastHostId(SecondaryStorageVm.Role role, long hostId);

    List<SecondaryStorageVmVO> listUpByHostId(SecondaryStorageVm.Role role, long hostId);

    List<SecondaryStorageVmVO> listByZoneId(SecondaryStorageVm.Role role, long zoneId);

    SecondaryStorageVmVO findByInstanceName(String instanceName);
}
