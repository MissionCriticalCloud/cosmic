package com.cloud.network.vpc;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;

public interface VpcOffering extends InternalIdentity, Identity {
    public static final String defaultVPCOfferingName = "Default VPC offering";
    public static final String defaultRemoteGatewayVPCOfferingName = "Default Remote Gateway VPC offering";
    public static final String defaultRemoteGatewayWithVPNVPCOfferingName = "Default Remote Gateway with VPN VPC offering";
    public static final String defaultInternalVPCOfferingName = "Default Internal VPC offering";
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
     * @return secondary service offering id used by VPC virutal router
     */
    Long getSecondaryServiceOfferingId();

    boolean getRedundantRouter();

    public enum State {
        Disabled, Enabled
    }
}
