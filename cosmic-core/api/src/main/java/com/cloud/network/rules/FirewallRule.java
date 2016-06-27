package com.cloud.network.rules;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Displayable;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.List;

public interface FirewallRule extends ControlledEntity, Identity, InternalIdentity, Displayable {
    /**
     * @return external id.
     */
    String getXid();

    /**
     * @return first port of the source port range.
     */
    Integer getSourcePortStart();

    /**
     * @return last port of the source prot range.  If this is null, that means only one port is mapped.
     */
    Integer getSourcePortEnd();

    /**
     * @return protocol to open these ports for.
     */
    String getProtocol();

    Purpose getPurpose();

    State getState();

    long getNetworkId();

    Long getSourceIpAddressId();

    Integer getIcmpCode();

    Integer getIcmpType();

    List<String> getSourceCidrList();

    Long getRelated();

    FirewallRuleType getType();

    /**
     * @return
     */
    TrafficType getTrafficType();

    @Override
    boolean isDisplay();

    enum Purpose {
        Firewall, PortForwarding, LoadBalancing, Vpn, StaticNat, NetworkACL,
    }

    enum FirewallRuleType {
        System, // The pre-defined rules created by admin, in the system wide
        User // the rules created by user, to a specific ip
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
