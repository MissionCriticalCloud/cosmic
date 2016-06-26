package com.cloud.network.dao;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = ("network_rule_config"))
public class NetworkRuleConfigVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "security_group_id")
    private long securityGroupId;

    @Column(name = "public_port")
    private String publicPort;

    @Column(name = "private_port")
    private String privatePort;

    @Column(name = "protocol")
    private String protocol;

    public NetworkRuleConfigVO() {
    }

    public NetworkRuleConfigVO(final long securityGroupId, final String publicPort, final String privatePort, final String protocol) {
        this.securityGroupId = securityGroupId;
        this.publicPort = publicPort;
        this.privatePort = privatePort;
        this.protocol = protocol;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getSecurityGroupId() {
        return securityGroupId;
    }

    public String getPublicPort() {
        return publicPort;
    }

    public String getPrivatePort() {
        return privatePort;
    }

    public String getProtocol() {
        return protocol;
    }
}
