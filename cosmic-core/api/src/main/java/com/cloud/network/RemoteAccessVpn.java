package com.cloud.network;

import com.cloud.api.Displayable;
import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

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
