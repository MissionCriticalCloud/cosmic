package com.cloud.db.model;

import com.cloud.model.LoadBalancerRuleBackend;
import com.cloud.model.enumeration.IpProtocol;
import com.cloud.model.enumeration.LoadBalancerAlgorithm;
import com.cloud.model.enumeration.LoadBalancerKind;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "load_balancer_rule")
public class LoadBalancerRule extends com.cloud.model.LoadBalancerRule {

    @Access(AccessType.PROPERTY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long getId() {
        return super.getId();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "uuid")
    public String getUuid() {
        return super.getUuid();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "account_id")
    public Long getAccountId() {
        return super.getAccountId();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "domain_id")
    public Long getDomainId() {
        return super.getDomainId();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "vpc_id")
    public Long getVpcId() {
        return super.getVpcId();
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
    @Column(name = "ipv4_address_id")
    public Long getIpv4AddressId() {
        return super.getIpv4AddressId();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "port")
    public Long getPort() {
        return super.getPort();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "ip_protocol")
    @Enumerated(value = EnumType.STRING)
    public IpProtocol getIpProtocol() {
        return super.getIpProtocol();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "load_balancer_algorithm")
    @Enumerated(value = EnumType.STRING)
    public LoadBalancerAlgorithm getLoadBalancingAlgorithm() {
        return super.getLoadBalancingAlgorithm();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "load_balancer_kind")
    @Enumerated(value = EnumType.STRING)
    public LoadBalancerKind getLoadBalancingKind() {
        return super.getLoadBalancingKind();
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "loadBalancerRule")
    public List<LoadBalancerRuleBackend> getLoadBalancingRuleBackends() {
        return super.getLoadBalancingRuleBackends();
    }
}
