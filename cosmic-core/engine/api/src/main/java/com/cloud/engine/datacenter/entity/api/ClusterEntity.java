package com.cloud.engine.datacenter.entity.api;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.model.enumeration.AllocationState;

public interface ClusterEntity extends DataCenterResourceEntity, OrganizationScope {

    long getDataCenterId();

    long getPodId();

    HypervisorType getHypervisorType();

    AllocationState getAllocationState();
}
