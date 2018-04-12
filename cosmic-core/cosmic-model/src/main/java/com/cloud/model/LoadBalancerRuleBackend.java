package com.cloud.model;

import java.util.Date;

public class LoadBalancerRuleBackend {
    private Long id;
    private Date created;
    private Date removed;

    private String ipv4Address;
    private Long port;
    private Long weight;

    private LoadBalancerRule loadBalancerRule;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public String getIpv4Address() {
        return ipv4Address;
    }

    public void setIpv4Address(final String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(final Long port) {
        this.port = port;
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(final Long weight) {
        this.weight = weight;
    }

    public LoadBalancerRule getLoadBalancerRule() {
        return loadBalancerRule;
    }

    public void setLoadBalancerRule(final LoadBalancerRule loadBalancerRule) {
        this.loadBalancerRule = loadBalancerRule;
    }
}
