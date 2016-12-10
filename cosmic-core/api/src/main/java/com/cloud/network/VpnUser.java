package com.cloud.network;

import com.cloud.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface VpnUser extends ControlledEntity, InternalIdentity, Identity {
    String getUsername();

    String getPassword();

    State getState();

    enum State {
        Add, Revoke, Active
    }
}
