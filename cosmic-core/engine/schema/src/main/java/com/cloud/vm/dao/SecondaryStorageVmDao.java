package com.cloud.vm.dao;

import com.cloud.utils.db.GenericDao;
import com.cloud.vm.SecondaryStorageVm;
import com.cloud.vm.SecondaryStorageVmVO;
import com.cloud.vm.VirtualMachine.State;

import java.util.List;

public interface SecondaryStorageVmDao extends GenericDao<SecondaryStorageVmVO, Long> {

    public List<SecondaryStorageVmVO> getSecStorageVmListInStates(SecondaryStorageVm.Role role, long dataCenterId, State... states);

    public List<SecondaryStorageVmVO> getSecStorageVmListInStates(SecondaryStorageVm.Role role, State... states);

    public List<SecondaryStorageVmVO> listByHostId(SecondaryStorageVm.Role role, long hostId);

    public List<SecondaryStorageVmVO> listByLastHostId(SecondaryStorageVm.Role role, long hostId);

    public List<SecondaryStorageVmVO> listUpByHostId(SecondaryStorageVm.Role role, long hostId);

    public List<SecondaryStorageVmVO> listByZoneId(SecondaryStorageVm.Role role, long zoneId);

    public List<Long> getRunningSecStorageVmListByMsid(SecondaryStorageVm.Role role, long msid);

    public List<Long> listRunningSecStorageOrderByLoad(SecondaryStorageVm.Role role, long zoneId);

    SecondaryStorageVmVO findByInstanceName(String instanceName);
}
