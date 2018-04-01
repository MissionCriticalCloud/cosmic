package com.cloud.storage;

import com.cloud.api.InternalIdentity;

import java.util.Date;

public interface GuestOSHypervisor extends InternalIdentity {

    String getHypervisorType();

    long getGuestOsId();

    String getHypervisorVersion();

    String getUuid();

    Date getRemoved();

    Date getCreated();

    boolean getIsUserDefined();
}
