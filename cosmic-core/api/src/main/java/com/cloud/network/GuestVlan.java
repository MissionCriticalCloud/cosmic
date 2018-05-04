package com.cloud.network;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;

public interface GuestVlan extends InternalIdentity, Identity {

    @Override
    public long getId();

    public long getAccountId();

    public String getGuestVlanRange();

    public long getPhysicalNetworkId();
}
