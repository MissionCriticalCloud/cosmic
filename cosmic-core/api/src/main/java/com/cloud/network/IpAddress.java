package com.cloud.network;

import com.cloud.utils.net.Ip;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Displayable;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

/**
 * - Allocated = null
 * - AccountId = null
 * - DomainId = null
 * <p>
 * - State = Allocated
 * - AccountId = account owner.
 * - DomainId = domain of the account owner.
 * - Allocated = time it was allocated.
 */
public interface IpAddress extends ControlledEntity, Identity, InternalIdentity, Displayable {
    long getDataCenterId();

    Ip getAddress();

    Date getAllocatedTime();

    boolean isSourceNat();

    long getVlanId();

    boolean isOneToOneNat();

    State getState();

    void setState(IpAddress.State state);

    boolean readyToUse();

    Long getAssociatedWithNetworkId();

    Long getAssociatedWithVmId();

    public Long getPhysicalNetworkId();

    Long getAllocatedToAccountId();

    Long getAllocatedInDomainId();

    boolean getSystem();

    Long getVpcId();

    String getVmIp();

    boolean isPortable();

    Long getNetworkId();

    boolean isDisplay();

    public Date getRemoved();

    public Date getCreated();

    enum State {
        Allocating, // The IP Address is being propagated to other network elements and is not ready for use yet.
        Allocated, // The IP address is in used.
        Releasing, // The IP address is being released for other network elements and is not ready for allocation.
        Free // The IP address is ready to be allocated.
    }

    enum Purpose {
        StaticNat, Lb
    }
}
