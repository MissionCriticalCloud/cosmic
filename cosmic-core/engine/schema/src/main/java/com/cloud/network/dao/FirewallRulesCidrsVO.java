package com.cloud.network.dao;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = ("firewall_rules_cidrs"))
public class FirewallRulesCidrsVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "firewall_rule_id")
    private long firewallRuleId;

    @Column(name = "source_cidr")
    private String sourceCidrList;

    public FirewallRulesCidrsVO() {
    }

    public FirewallRulesCidrsVO(final long firewallRuleId, final String sourceCidrList) {
        this.firewallRuleId = firewallRuleId;
        this.sourceCidrList = sourceCidrList;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getFirewallRuleId() {
        return firewallRuleId;
    }

    public String getCidr() {
        return sourceCidrList;
    }

    public String getSourceCidrList() {
        return sourceCidrList;
    }

    public void setSourceCidrList(final String sourceCidrList) {
        this.sourceCidrList = sourceCidrList;
    }
}
