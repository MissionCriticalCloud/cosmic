package com.cloud.network;

import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.network.Network.Service;

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

    List<Service> getEnabledServices();

    String getUuid();

    boolean isNetworkAclServiceProvided();

    public enum State {
        Disabled, Enabled, Shutdown
    }
}
