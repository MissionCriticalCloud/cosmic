package com.cloud.legacymodel.dc;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;

import java.util.Date;

public interface Vlan extends InternalIdentity, Identity {
    String UNTAGGED = "untagged";

    String getVlanTag();

    String getVlanGateway();

    String getVlanNetmask();

    long getDataCenterId();

    String getIpRange();

    VlanType getVlanType();

    Long getNetworkId();

    Date getRemoved();

    Date getCreated();

    Long getPhysicalNetworkId();

    String getIp6Gateway();

    String getIp6Cidr();

    String getIp6Range();

    enum VlanType {
        DirectAttached, VirtualNetwork
    }
}
