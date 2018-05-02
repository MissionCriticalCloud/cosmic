package com.cloud.legacymodel.network.vpc;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;

public interface VpcOffering extends InternalIdentity, Identity {
    String defaultVPCOfferingName = "Default VPC offering";
    String defaultRemoteGatewayVPCOfferingName = "Default Remote Gateway VPC offering";
    String defaultRemoteGatewayWithVPNVPCOfferingName = "Default Remote Gateway with VPN VPC offering";
    String defaultInternalVPCOfferingName = "Default Internal VPC offering";
    String redundantVPCOfferingName = "Redundant VPC offering";

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

    enum State {
        Disabled, Enabled
    }
}
