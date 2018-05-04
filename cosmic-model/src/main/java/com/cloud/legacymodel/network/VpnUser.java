package com.cloud.legacymodel.network;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

public interface VpnUser extends ControlledEntity, InternalIdentity, Identity {
    String getUsername();

    String getPassword();

    State getState();

    enum State {
        Add, Revoke, Active
    }
}
