package com.cloud.api.response;

import com.cloud.network.security.SecurityRule.SecurityRuleType;
import com.cloud.serializer.Param;

public class SecurityGroupRuleResultObject {
    @Param(name = "id")
    private Long id;

    @Param(name = "startport")
    private int startPort;

    @Param(name = "endport")
    private int endPort;

    @Param(name = "protocol")
    private String protocol;

    @Param(name = "securitygroup")
    private String allowedSecurityGroup = null;

    @Param(name = "account")
    private String allowedSecGroupAcct = null;

    @Param(name = "cidr")
    private String allowedSourceIpCidr = null;

    private SecurityRuleType type;

    public SecurityGroupRuleResultObject() {
    }

    public SecurityGroupRuleResultObject(final Long id, final int startPort, final int endPort, final String protocol, final String allowedSecurityGroup, final String
            allowedSecGroupAcct,
                                         final String allowedSourceIpCidr) {
        this.id = id;
        this.startPort = startPort;
        this.endPort = endPort;
        this.protocol = protocol;
        this.allowedSecurityGroup = allowedSecurityGroup;
        this.allowedSecGroupAcct = allowedSecGroupAcct;
        this.allowedSourceIpCidr = allowedSourceIpCidr;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public int getStartPort() {
        return startPort;
    }

    public void setStartPort(final int startPort) {
        this.startPort = startPort;
    }

    public SecurityRuleType getRuleType() {
        return type;
    }

    public void setRuleType(final SecurityRuleType type) {
        this.type = type;
    }

    public int getEndPort() {
        return endPort;
    }

    public void setEndPort(final int endPort) {
        this.endPort = endPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public String getAllowedSecurityGroup() {
        return allowedSecurityGroup;
    }

    public void setAllowedSecurityGroup(final String allowedSecurityGroup) {
        this.allowedSecurityGroup = allowedSecurityGroup;
    }

    public String getAllowedSecGroupAcct() {
        return allowedSecGroupAcct;
    }

    public void setAllowedSecGroupAcct(final String allowedSecGroupAcct) {
        this.allowedSecGroupAcct = allowedSecGroupAcct;
    }

    public String getAllowedSourceIpCidr() {
        return allowedSourceIpCidr;
    }

    public void setAllowedSourceIpCidr(final String allowedSourceIpCidr) {
        this.allowedSourceIpCidr = allowedSourceIpCidr;
    }
}
