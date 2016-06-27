package org.apache.cloudstack.engine.datacenter.entity.api;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.org.Cluster.ClusterType;
import com.cloud.org.Grouping.AllocationState;
import com.cloud.org.Managed.ManagedState;

public interface ClusterEntity extends DataCenterResourceEntity, OrganizationScope {

    long getDataCenterId();

    long getPodId();

    HypervisorType getHypervisorType();

    ClusterType getClusterType();

    AllocationState getAllocationState();

    ManagedState getManagedState();
}
