package com.cloud.hypervisor;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;
import com.cloud.hypervisor.Hypervisor.HypervisorType;

/**
 * HypervisorCapability represents one particular hypervisor version's capabilities.
 */
public interface HypervisorCapabilities extends Identity, InternalIdentity {

    /**
     * @return type of hypervisor
     */
    HypervisorType getHypervisorType();

    String getHypervisorVersion();

    boolean isSecurityGroupEnabled();

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
