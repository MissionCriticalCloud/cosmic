package com.cloud.network.security;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = ("security_group_rule"))
public class SecurityGroupRuleVO implements SecurityRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "security_group_id")
    private long securityGroupId;

    @Column(name = "start_port")
    private int startPort;

    @Column(name = "end_port")
    private int endPort;

    @Column(name = "type")
    private String type;

    @Column(name = "protocol")
    private String protocol;

    @Column(name = "allowed_network_id", nullable = true)
    private Long allowedNetworkId = null;

    @Column(name = "allowed_ip_cidr", nullable = true)
    private String allowedSourceIpCidr = null;

    @Column(name = "uuid")
    private String uuid;

    public SecurityGroupRuleVO() {
        uuid = UUID.randomUUID().toString();
    }

    public SecurityGroupRuleVO(final SecurityRuleType type, final long securityGroupId, final int fromPort, final int toPort, final String protocol, final long allowedNetworkId) {
        this.securityGroupId = securityGroupId;
        startPort = fromPort;
        endPort = toPort;
        this.protocol = protocol;
        this.allowedNetworkId = allowedNetworkId;
        uuid = UUID.randomUUID().toString();
        if (type == SecurityRuleType.IngressRule) {
            this.type = SecurityRuleType.IngressRule.getType();
        } else {
            this.type = SecurityRuleType.EgressRule.getType();
        }
    }

    public SecurityGroupRuleVO(final SecurityRuleType type, final long securityGroupId, final int fromPort, final int toPort, final String protocol, final String allowedIpCidr) {
        this.securityGroupId = securityGroupId;
        startPort = fromPort;
        endPort = toPort;
        this.protocol = protocol;
        allowedSourceIpCidr = allowedIpCidr;
        uuid = UUID.randomUUID().toString();
        if (type == SecurityRuleType.IngressRule) {
            this.type = SecurityRuleType.IngressRule.getType();
        } else {
            this.type = SecurityRuleType.EgressRule.getType();
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getSecurityGroupId() {
        return securityGroupId;
    }

    @Override
    public int getStartPort() {
        return startPort;
    }

    @Override
    public int getEndPort() {
        return endPort;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public SecurityRuleType getRuleType() {
        if ("ingress".equalsIgnoreCase(type)) {
            return SecurityRuleType.IngressRule;
        } else {
            return SecurityRuleType.EgressRule;
        }
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public Long getAllowedNetworkId() {
        return allowedNetworkId;
    }

    @Override
    public String getAllowedSourceIpCidr() {
        return allowedSourceIpCidr;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
