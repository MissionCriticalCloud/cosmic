package com.cloud.legacymodel.dc;

import com.cloud.legacymodel.Identity;
import com.cloud.legacymodel.InternalIdentity;
import com.cloud.model.enumeration.AllocationState;
import com.cloud.model.enumeration.NetworkType;

import java.util.Map;

public interface DataCenter extends Identity, InternalIdentity {

    String getDns1();

    String getDns2();

    String getIp6Dns1();

    String getIp6Dns2();

    String getGuestNetworkCidr();

    String getName();

    Long getDomainId();

    String getDescription();

    String getDomain();

    NetworkType getNetworkType();

    String getInternalDns1();

    String getInternalDns2();

    Map<String, String> getDetails();

    void setDetails(Map<String, String> details);

    AllocationState getAllocationState();

    String getZoneToken();
}
