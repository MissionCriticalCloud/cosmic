package com.cloud.network;

import com.cloud.api.Displayable;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

import java.util.Date;

public interface Site2SiteVpnConnection extends ControlledEntity, InternalIdentity, Displayable {
    @Override
    public long getId();

    public String getUuid();

    public long getVpnGatewayId();

    public long getCustomerGatewayId();

    public State getState();

    public Date getCreated();

    public Date getRemoved();

    public boolean isPassive();

    @Override
    boolean isDisplay();

    enum State {
        Pending, Connected, Disconnected, Error,
    }
}
