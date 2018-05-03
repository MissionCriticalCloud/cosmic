package com.cloud.network;

import com.cloud.legacymodel.Displayable;
import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;
import com.cloud.legacymodel.network.Ip;

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

    Long getPhysicalNetworkId();

    Long getIpACLId();

    Long getAllocatedToAccountId();

    Long getAllocatedInDomainId();

    boolean getSystem();

    Long getVpcId();

    String getVmIp();

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
