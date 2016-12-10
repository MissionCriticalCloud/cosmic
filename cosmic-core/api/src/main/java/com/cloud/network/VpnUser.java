package com.cloud.network;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

public interface VpnUser extends ControlledEntity, InternalIdentity, Identity {
    String getUsername();

    String getPassword();

    State getState();

    enum State {
        Add, Revoke, Active
    }
}
