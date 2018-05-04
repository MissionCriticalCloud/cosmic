package com.cloud.network;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;

import java.util.Date;

public interface Site2SiteCustomerGateway extends ControlledEntity, Identity, InternalIdentity {
    public String getGatewayIp();

    public String getGuestCidrList();

    public String getIpsecPsk();

    public String getIkePolicy();

    public String getEspPolicy();

    public Long getIkeLifetime();

    public Long getEspLifetime();

    public Boolean getDpd();

    public Boolean getEncap();

    public Date getRemoved();

    String getName();
}
