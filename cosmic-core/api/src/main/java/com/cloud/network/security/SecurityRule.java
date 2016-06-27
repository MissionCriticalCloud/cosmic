package com.cloud.network.security;

import org.apache.cloudstack.api.Identity;
import org.apache.cloudstack.api.InternalIdentity;

public interface SecurityRule extends Identity, InternalIdentity {

    long getSecurityGroupId();

    int getStartPort();

    int getEndPort();

    String getType();

    SecurityRuleType getRuleType();

    String getProtocol();

    Long getAllowedNetworkId();

    String getAllowedSourceIpCidr();

    public static class SecurityRuleType {
        public static final SecurityRuleType IngressRule = new SecurityRuleType("ingress");
        public static final SecurityRuleType EgressRule = new SecurityRuleType("egress");
        private final String _type;

        public SecurityRuleType(final String type) {
            _type = type;
        }

        public String getType() {
            return _type;
        }
    }
}
