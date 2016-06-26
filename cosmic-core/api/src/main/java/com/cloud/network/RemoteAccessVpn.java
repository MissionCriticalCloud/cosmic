package com.cloud.network;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Displayable;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface RemoteAccessVpn extends ControlledEntity, InternalIdentity, Identity, Displayable {
    long getServerAddressId();

    String getIpRange();

    String getIpsecPresharedKey();

    String getLocalIp();

    Long getNetworkId();

    Long getVpcId();

    State getState();

    @Override
    boolean isDisplay();

    enum State {
        Added, Running, Removed
    }
}
