package com.cloud.org;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.org.Managed.ManagedState;

public interface Cluster extends Grouping, InternalIdentity, Identity {
    String getName();

    long getDataCenterId();

    long getPodId();

    HypervisorType getHypervisorType();

    ClusterType getClusterType();

    AllocationState getAllocationState();

    ManagedState getManagedState();

    public static enum ClusterType {
        CloudManaged, ExternalManaged
    }
}
