package com.cloud.network.security;

import com.cloud.network.security.SecurityRule.SecurityRuleType;
import org.apache.cloudstack.api.InternalIdentity;

public interface SecurityGroupRules extends InternalIdentity {

    String getName();

    String getDescription();

    Long getDomainId();

    Long getAccountId();

    Long getRuleId();

    String getRuleUuid();

    int getStartPort();

    int getEndPort();

    String getProtocol();

    Long getAllowedNetworkId();

    String getAllowedSourceIpCidr();

    SecurityRuleType getRuleType();
}
