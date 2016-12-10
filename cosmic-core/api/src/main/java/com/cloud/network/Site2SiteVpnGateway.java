package com.cloud.network;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.Displayable;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;

import java.util.Date;

public interface Site2SiteVpnGateway extends ControlledEntity, Identity, InternalIdentity, Displayable {
    public long getAddrId();

    public long getVpcId();

    public Date getRemoved();

    @Override
    boolean isDisplay();
}
