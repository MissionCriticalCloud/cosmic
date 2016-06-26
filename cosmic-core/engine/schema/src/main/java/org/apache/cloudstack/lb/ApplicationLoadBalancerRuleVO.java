package org.apache.cloudstack.lb;

import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.utils.net.Ip;
import com.cloud.utils.net.NetUtils;
import org.apache.cloudstack.network.lb.ApplicationLoadBalancerRule;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * This VO represent Internal Load Balancer rule.
 * Instead of pointing to the public ip address id directly as External Load Balancer rule does, it refers to the ip address by its value/sourceNetworkid
 */
@Entity
@Table(name = ("load_balancing_rules"))
@DiscriminatorValue(value = "LoadBalancing")
@PrimaryKeyJoinColumn(name = "id")
public class ApplicationLoadBalancerRuleVO extends FirewallRuleVO implements ApplicationLoadBalancerRule {
    @Column(name = "source_ip_address_network_id")
    Long sourceIpNetworkId;
    @Column(name = "lb_protocol")
    String lbProtocol;
    @Enumerated(value = EnumType.STRING)
    @Column(name = "scheme")
    Scheme scheme;
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
    @Column(name = "source_ip_address")
    @Enumerated(value = EnumType.STRING)
    private Ip sourceIp = null;

    public ApplicationLoadBalancerRuleVO() {
    }

    public ApplicationLoadBalancerRuleVO(final String name, final String description, final int srcPort, final int instancePort, final String algorithm, final long networkId,
                                         final long accountId, final long domainId,
                                         final Ip sourceIp, final long sourceIpNtwkId, final Scheme scheme) {
        super(null, null, srcPort, srcPort, NetUtils.TCP_PROTO, networkId, accountId, domainId, Purpose.LoadBalancing, null, null, null, null, null);

        this.name = name;
        this.description = description;
        this.algorithm = algorithm;
        this.defaultPortStart = instancePort;
        this.defaultPortEnd = instancePort;
        this.sourceIp = sourceIp;
        this.sourceIpNetworkId = sourceIpNtwkId;
        this.scheme = scheme;
    }

    @Override
    public Long getSourceIpNetworkId() {
        return sourceIpNetworkId;
    }

    @Override
    public Ip getSourceIp() {
        return sourceIp;
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

    @Override
    public Scheme getScheme() {
        return scheme;
    }

    @Override
    public int getDefaultPortStart() {
        return defaultPortStart;
    }

    @Override
    public int getDefaultPortEnd() {
        return defaultPortEnd;
    }

    @Override
    public int getInstancePort() {
        return defaultPortStart;
    }
}
