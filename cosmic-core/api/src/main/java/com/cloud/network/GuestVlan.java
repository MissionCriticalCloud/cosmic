package com.cloud.network;

import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

public interface GuestVlan extends InternalIdentity, Identity {

    @Override
    public long getId();

    public long getAccountId();

    public String getGuestVlanRange();

    public long getPhysicalNetworkId();
}
