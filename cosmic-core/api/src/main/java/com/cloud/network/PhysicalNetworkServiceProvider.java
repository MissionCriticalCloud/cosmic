package com.cloud.network;

import com.cloud.network.Network.Service;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.List;

/**
 *
 */
public interface PhysicalNetworkServiceProvider extends InternalIdentity {

    @Override
    long getId();

    State getState();

    void setState(State state);

    long getPhysicalNetworkId();

    String getProviderName();

    long getDestinationPhysicalNetworkId();

    boolean isLbServiceProvided();

    boolean isVpnServiceProvided();

    boolean isDhcpServiceProvided();

    boolean isDnsServiceProvided();

    boolean isGatewayServiceProvided();

    boolean isFirewallServiceProvided();

    boolean isSourcenatServiceProvided();

    boolean isUserdataServiceProvided();

    boolean isSecuritygroupServiceProvided();

    List<Service> getEnabledServices();

    String getUuid();

    boolean isNetworkAclServiceProvided();

    public enum State {
        Disabled, Enabled, Shutdown
    }
}
