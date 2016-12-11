package com.cloud.network.security;

import com.cloud.api.InternalIdentity;
import com.cloud.network.security.SecurityRule.SecurityRuleType;

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
