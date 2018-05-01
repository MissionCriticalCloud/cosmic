package com.cloud.legacymodel.dc;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.model.enumeration.AllocationState;
import com.cloud.model.enumeration.HypervisorType;
import com.cloud.model.enumeration.ManagedState;

public interface Cluster extends InternalIdentity, Identity {
    String getName();

    long getDataCenterId();

    long getPodId();

    HypervisorType getHypervisorType();

    ClusterType getClusterType();

    AllocationState getAllocationState();

    ManagedState getManagedState();

    enum ClusterType {
        CloudManaged,
        ExternalManaged
    }
}
