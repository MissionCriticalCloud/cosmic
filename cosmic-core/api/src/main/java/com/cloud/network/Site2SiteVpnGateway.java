package com.cloud.network;

import com.cloud.api.Displayable;
import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

import java.util.Date;

public interface Site2SiteVpnGateway extends ControlledEntity, Identity, InternalIdentity, Displayable {
    public long getAddrId();

    public long getVpcId();

    public Date getRemoved();

    @Override
    boolean isDisplay();
}
