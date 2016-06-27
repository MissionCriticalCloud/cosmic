package com.cloud.hypervisor.dao;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.HypervisorCapabilitiesVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface HypervisorCapabilitiesDao extends GenericDao<HypervisorCapabilitiesVO, Long> {

    List<HypervisorCapabilitiesVO> listAllByHypervisorType(HypervisorType hypervisorType);

    HypervisorCapabilitiesVO findByHypervisorTypeAndVersion(HypervisorType hypervisorType, String hypervisorVersion);

    Long getMaxGuestsLimit(HypervisorType hypervisorType, String hypervisorVersion);

    Integer getMaxDataVolumesLimit(HypervisorType hypervisorType, String hypervisorVersion);

    Integer getMaxHostsPerCluster(HypervisorType hypervisorType, String hypervisorVersion);

    Boolean isVmSnapshotEnabled(HypervisorType hypervisorType, String hypervisorVersion);
}
