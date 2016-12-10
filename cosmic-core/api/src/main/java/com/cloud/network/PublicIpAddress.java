package com.cloud.network;

import com.cloud.acl.ControlledEntity;
import com.cloud.api.InternalIdentity;
import com.cloud.dc.Vlan;

public interface PublicIpAddress extends ControlledEntity, IpAddress, Vlan, InternalIdentity {

    String getMacAddress();

    String getNetmask();

    String getGateway();
}
