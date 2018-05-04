package com.cloud.legacymodel.user;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

import java.util.Date;

public interface Account extends ControlledEntity, InternalIdentity, Identity {

    short ACCOUNT_TYPE_NORMAL = 0;
    short ACCOUNT_TYPE_ADMIN = 1;
    short ACCOUNT_TYPE_DOMAIN_ADMIN = 2;
    short ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN = 3;
    short ACCOUNT_TYPE_READ_ONLY_ADMIN = 4;
    short ACCOUNT_TYPE_PROJECT = 5;
    long ACCOUNT_ID_SYSTEM = 1;

    String getAccountName();

    short getType();

    State getState();

    Date getRemoved();

    String getNetworkDomain();

    boolean isDefault();

    enum State {
        disabled, enabled, locked
    }
}
