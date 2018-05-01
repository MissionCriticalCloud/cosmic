package com.cloud.org;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.model.enumeration.AllocationState;
import com.cloud.org.Managed.ManagedState;

public interface Cluster extends InternalIdentity, Identity {
    String getName();

    long getDataCenterId();

    long getPodId();

    HypervisorType getHypervisorType();

    ClusterType getClusterType();

    AllocationState getAllocationState();

    ManagedState getManagedState();

    enum ClusterType {
        CloudManaged, ExternalManaged
    }
}
