package com.cloud.network;

import com.cloud.dc.Vlan;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.InternalIdentity;

/**
 */
public interface PublicIpAddress extends ControlledEntity, IpAddress, Vlan, InternalIdentity {

    String getMacAddress();

    public String getNetmask();

    public String getGateway();
}
