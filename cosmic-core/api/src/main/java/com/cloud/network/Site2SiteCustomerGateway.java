package com.cloud.network;

import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

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
