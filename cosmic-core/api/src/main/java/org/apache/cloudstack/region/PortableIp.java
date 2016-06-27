package org.apache.cloudstack.region;

import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface PortableIp extends InternalIdentity {

    Long getAllocatedToAccountId();

    Long getAllocatedInDomainId();

    Date getAllocatedTime();

    State getState();

    int getRegionId();

    Long getAssociatedDataCenterId();

    Long getAssociatedWithNetworkId();

    Long getAssociatedWithVpcId();

    Long getPhysicalNetworkId();

    String getAddress();

    String getVlan();

    String getNetmask();

    String getGateway();

    enum State {
        Allocating, // The IP Address is being propagated to other network elements and is not ready for use yet.
        Allocated,  // The IP address is in used.
        Releasing,  // The IP address is being released for other network elements and is not ready for allocation.
        Free        // The IP address is ready to be allocated.
    }
}
