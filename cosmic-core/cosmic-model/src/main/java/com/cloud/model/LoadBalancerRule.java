package com.cloud.model;

import com.cloud.model.enumeration.IpProtocol;
import com.cloud.model.enumeration.LoadBalancerAlgorithm;
import com.cloud.model.enumeration.LoadBalancerKind;

import java.util.Date;
import java.util.List;

public class LoadBalancerRule {
    private Long id;
    private String uuid;
    private Long accountId;
    private Long domainId;
    private Long vpcId;
    private Date created;
    private Date removed;

    private Long ipv4AddressId;
    private Long port;
    private IpProtocol ipProtocol;
    private LoadBalancerAlgorithm loadBalancingAlgorithm;
    private LoadBalancerKind loadBalancingKind;

    private List<LoadBalancerRuleBackend> loadBalancingRuleBackends;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(final Long domainId) {
        this.domainId = domainId;
    }

    public Long getVpcId() {
        return vpcId;
    }

    public void setVpcId(final Long vpcId) {
        this.vpcId = vpcId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(final Date removed) {
        this.removed = removed;
    }

    public Long getIpv4AddressId() {
        return ipv4AddressId;
    }

    public void setIpv4AddressId(final Long ipv4AddressId) {
        this.ipv4AddressId = ipv4AddressId;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(final Long port) {
        this.port = port;
    }

    public IpProtocol getIpProtocol() {
        return ipProtocol;
    }

    public void setIpProtocol(final IpProtocol ipProtocol) {
        this.ipProtocol = ipProtocol;
    }

    public LoadBalancerAlgorithm getLoadBalancingAlgorithm() {
        return loadBalancingAlgorithm;
    }

    public void setLoadBalancingAlgorithm(final LoadBalancerAlgorithm loadBalancingAlgorithm) {
        this.loadBalancingAlgorithm = loadBalancingAlgorithm;
    }

    public LoadBalancerKind getLoadBalancingKind() {
        return loadBalancingKind;
    }

    public void setLoadBalancingKind(final LoadBalancerKind loadBalancingKind) {
        this.loadBalancingKind = loadBalancingKind;
    }

    public List<LoadBalancerRuleBackend> getLoadBalancingRuleBackends() {
        return loadBalancingRuleBackends;
    }

    public void setLoadBalancingRuleBackends(final List<LoadBalancerRuleBackend> loadBalancingRuleBackends) {
        this.loadBalancingRuleBackends = loadBalancingRuleBackends;
    }
}
