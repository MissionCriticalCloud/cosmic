package com.cloud.hypervisor;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.model.enumeration.HypervisorType;

/**
 * HypervisorCapability represents one particular hypervisor version's capabilities.
 */
public interface HypervisorCapabilities extends Identity, InternalIdentity {

    /**
     * @return type of hypervisor
     */
    HypervisorType getHypervisorType();

    String getHypervisorVersion();

    /**
     * @return the maxGuestslimit
     */
    Long getMaxGuestsLimit();

    /**
     * @return the max. data volumes per VM supported by hypervisor
     */
    Integer getMaxDataVolumesLimit();

    /**
     * @return the max. hosts per cluster supported by hypervisor
     */
    Integer getMaxHostsPerCluster();

    boolean isStorageMotionSupported();
}
