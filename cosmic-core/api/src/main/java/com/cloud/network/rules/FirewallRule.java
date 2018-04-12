package com.cloud.network.rules;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Displayable;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

import java.util.List;

public interface FirewallRule extends ControlledEntity, Identity, InternalIdentity, Displayable {
    String getXid();

    Integer getSourcePort();

    String getProtocol();

    Purpose getPurpose();

    State getState();

    long getNetworkId();

    Long getSourceIpAddressId();

    Integer getIcmpCode();

    Integer getIcmpType();

    List<String> getSourceCidrList();

    TrafficType getTrafficType();

    @Override
    boolean isDisplay();

    enum Purpose {
        PortForwarding,
        LoadBalancing,
        Vpn,
        StaticNat,
        NetworkACL,
    }

    enum State {
        Staged, // Rule been created but has never got through network rule conflict detection.  Rules in this state can not be sent to network elements.
        Add,    // Add means the rule has been created and has gone through network rule conflict detection.
        Active, // Rule has been sent to the network elements and reported to be active.
        Revoke,  // Revoke means this rule has been revoked. If this rule has been sent to the network elements, the rule will be deleted from database.
        Deleting // rule has been revoked and is scheduled for deletion
    }

    enum TrafficType {
        Ingress, Egress
    }
}
