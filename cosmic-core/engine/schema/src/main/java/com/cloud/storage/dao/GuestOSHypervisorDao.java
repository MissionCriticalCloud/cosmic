package com.cloud.storage.dao;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.GuestOSHypervisorVO;
import com.cloud.utils.db.GenericDao;

public interface GuestOSHypervisorDao extends GenericDao<GuestOSHypervisorVO, Long> {

    HypervisorType findHypervisorTypeByGuestOsId(long guestOsId);

    GuestOSHypervisorVO findByOsIdAndHypervisor(long guestOsId, String hypervisorType, String hypervisorVersion);

    boolean removeGuestOsMapping(Long id);

    GuestOSHypervisorVO findByOsIdAndHypervisorAndUserDefined(long guestOsId, String hypervisorType, String hypervisorVersion, boolean isUserDefined);
}
