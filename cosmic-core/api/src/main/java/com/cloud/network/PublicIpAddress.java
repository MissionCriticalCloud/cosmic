package com.cloud.network;

import com.cloud.acl.ControlledEntity;
import com.cloud.dc.Vlan;
import org.apache.cloudstack.api.InternalIdentity;

public interface PublicIpAddress extends ControlledEntity, IpAddress, Vlan, InternalIdentity {

    String getMacAddress();

    String getNetmask();

    String getGateway();
}
