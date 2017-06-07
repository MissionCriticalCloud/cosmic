package com.cloud.dc;

import com.cloud.acl.InfrastructureEntity;
import com.cloud.api.Identity;
import com.cloud.api.InternalIdentity;
import com.cloud.model.enumeration.AllocationState;
import com.cloud.model.enumeration.NetworkType;

import java.util.Map;

public interface DataCenter extends InfrastructureEntity, Identity, InternalIdentity {

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

    String getDnsProvider();

    String getGatewayProvider();

    String getFirewallProvider();

    String getDhcpProvider();

    String getLoadBalancerProvider();

    String getUserDataProvider();

    String getVpnProvider();

    boolean isSecurityGroupEnabled();

    Map<String, String> getDetails();

    void setDetails(Map<String, String> details);

    AllocationState getAllocationState();

    String getZoneToken();

    boolean isLocalStorageEnabled();
}
