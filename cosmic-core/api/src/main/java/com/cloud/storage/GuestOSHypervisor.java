package com.cloud.storage;

import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface GuestOSHypervisor extends InternalIdentity {

    String getHypervisorType();

    String getGuestOsName();

    long getGuestOsId();

    String getHypervisorVersion();

    String getUuid();

    Date getRemoved();

    Date getCreated();

    boolean getIsUserDefined();
}
