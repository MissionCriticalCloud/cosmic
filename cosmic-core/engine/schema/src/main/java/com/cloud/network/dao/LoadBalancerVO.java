package com.cloud.network.dao;

import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.utils.net.NetUtils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * This VO represent Public Load Balancer
 * It references source ip address by its Id.
 * To get the VO for Internal Load Balancer rule, please refer to LoadBalancerRuleVO
 */
@Entity
@Table(name = ("load_balancing_rules"))
@DiscriminatorValue(value = "LoadBalancing")
@PrimaryKeyJoinColumn(name = "id")
public class LoadBalancerVO extends FirewallRuleVO implements LoadBalancer {

    @Enumerated(value = EnumType.STRING)
    @Column(name = "scheme")
    Scheme scheme = Scheme.Public;
    @Column(name = "lb_protocol")
    String lbProtocol;
    @Column(name = "name")
    private String name;
    @Column(name = "description", length = 4096)
    private String description;
    @Column(name = "algorithm")
    private String algorithm;
    @Column(name = "default_port_start")
    private int defaultPortStart;
    @Column(name = "default_port_end")
    private int defaultPortEnd;

    public LoadBalancerVO() {
    }

    public LoadBalancerVO(final String xId, final String name, final String description, final long srcIpId, final int srcPort, final int dstPort, final String algorithm, final
    long networkId, final long accountId,
                          final long domainId, final String lbProtocol) {
        super(xId, srcIpId, srcPort, NetUtils.TCP_PROTO, networkId, accountId, domainId, Purpose.LoadBalancing, null, null, null, null);
        this.name = name;
        this.description = description;
        this.algorithm = algorithm;
        this.defaultPortStart = dstPort;
        this.defaultPortEnd = dstPort;
        this.scheme = Scheme.Public;
        this.lbProtocol = lbProtocol;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public String getLbProtocol() {
        return lbProtocol;
    }

    public void setLbProtocol(final String lbProtocol) {
        this.lbProtocol = lbProtocol;
    }

    @Override
    public Scheme getScheme() {
        return scheme;
    }

    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public int getDefaultPortStart() {
        return defaultPortStart;
    }

    @Override
    public int getDefaultPortEnd() {
        return defaultPortEnd;
    }
}
