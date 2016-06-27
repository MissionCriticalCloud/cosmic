package com.cloud.network;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Displayable;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

import java.util.Date;

public interface Site2SiteVpnGateway extends ControlledEntity, Identity, InternalIdentity, Displayable {
    public long getAddrId();

    public long getVpcId();

    public Date getRemoved();

    @Override
    boolean isDisplay();
}
