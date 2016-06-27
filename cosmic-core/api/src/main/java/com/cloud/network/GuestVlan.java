package com.cloud.network;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface GuestVlan extends InternalIdentity, Identity {

    @Override
    public long getId();

    public long getAccountId();

    public String getGuestVlanRange();

    public long getPhysicalNetworkId();
}
