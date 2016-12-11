package com.cloud.network;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Displayable;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

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
