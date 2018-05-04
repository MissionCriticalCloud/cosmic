package com.cloud.network;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

/**
 * @author Sheng Yang
 */
public interface UserIpv6Address extends ControlledEntity, Identity, InternalIdentity {
    long getDataCenterId();

    String getAddress();

    long getVlanId();

    State getState();

    void setState(UserIpv6Address.State state);

    Long getNetworkId();

    Long getSourceNetworkId();

    Long getPhysicalNetworkId();

    String getMacAddress();

    enum State {
        Allocating, // The IP Address is being propagated to other network elements and is not ready for use yet.
        Allocated, // The IP address is in used.
        Releasing, // The IP address is being released for other network elements and is not ready for allocation.
        Free // The IP address is ready to be allocated.
    }
}
