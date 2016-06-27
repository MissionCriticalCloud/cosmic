package com.cloud.network.vpc;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface VpcOffering extends InternalIdentity, Identity {
    public static final String defaultVPCOfferingName = "Default VPC offering";
    public static final String redundantVPCOfferingName = "Redundant VPC offering";

    /**
     * @return VPC offering name
     */
    String getName();

    /**
     * @return VPC offering display text
     */
    String getDisplayText();

    /**
     * @return VPC offering state
     */
    State getState();

    /**
     * @return true if offering is default - came with the cloudStack fresh install; false otherwise
     */
    boolean isDefault();

    /**
     * @return service offering id used by VPC virutal router
     */
    Long getServiceOfferingId();

    /**
     * @return true if the offering provides a distributed router capable of one-hop forwarding
     */
    boolean supportsDistributedRouter();

    /**
     * @return true if VPC created with the offering can span multiple zones in the region
     */
    boolean offersRegionLevelVPC();

    boolean getRedundantRouter();

    public enum State {
        Disabled, Enabled
    }
}
