package com.cloud.db.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "load_balancer_rule_backend")
public class LoadBalancerRuleBackend extends com.cloud.model.LoadBalancerRuleBackend {

    @Access(AccessType.PROPERTY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long getId() {
        return super.getId();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "created")
    public Date getCreated() {
        return super.getCreated();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "removed")
    public Date getRemoved() {
        return super.getRemoved();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "ipv4_address")
    public String getIpv4Address() {
        return super.getIpv4Address();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "port")
    public Long getPort() {
        return super.getPort();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "weight")
    public Long getWeight() {
        return super.getWeight();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "load_balancer_rule_id", nullable = false)
    public com.cloud.model.LoadBalancerRule getLoadBalancerRule() {
        return super.getLoadBalancerRule();
    }
}
