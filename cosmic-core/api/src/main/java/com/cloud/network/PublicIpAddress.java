package com.cloud.network;

import com.cloud.legacymodel.InternalIdentity;
import com.cloud.legacymodel.acl.ControlledEntity;
import com.cloud.legacymodel.dc.Vlan;

public interface PublicIpAddress extends ControlledEntity, IpAddress, Vlan, InternalIdentity {

    String getMacAddress();

    String getNetmask();

    String getGateway();
}
