package com.cloud.network.vpc;

import org.apache.cloudstack.api.Displayable;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.List;

public interface NetworkACLItem extends InternalIdentity, Identity, Displayable {

    String getUuid();

    Action getAction();

    int getNumber();

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

    State getState();

    long getAclId();

    Integer getIcmpCode();

    Integer getIcmpType();

    List<String> getSourceCidrList();

    /**
     * @return
     */
    TrafficType getTrafficType();

    @Override
    boolean isDisplay();

    enum State {
        Staged, // Rule been created but has never got through network rule conflict detection.  Rules in this state can not be sent to network elements.
        Add,    // Add means the rule has been created and has gone through network rule conflict detection.
        Active, // Rule has been sent to the network elements and reported to be active.
        Revoke  // Revoke means this rule has been revoked. If this rule has been sent to the network elements, the rule will be deleted from database.
    }

    enum TrafficType {
        Ingress, Egress
    }

    enum Action {
        Allow, Deny
    }
}
