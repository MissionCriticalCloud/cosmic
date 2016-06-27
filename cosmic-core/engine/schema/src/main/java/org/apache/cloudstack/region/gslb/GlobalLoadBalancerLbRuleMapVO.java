package org.apache.cloudstack.region.gslb;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = ("global_load_balancer_lb_rule_map"))
public class GlobalLoadBalancerLbRuleMapVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "lb_rule_id")
    private long loadBalancerId;

    @Column(name = "gslb_rule_id")
    private long gslbLoadBalancerId;

    @Column(name = "weight")
    private long weight;

    @Column(name = "revoke")
    private boolean revoke = false;

    public GlobalLoadBalancerLbRuleMapVO() {
        this.weight = 1;
    }

    public GlobalLoadBalancerLbRuleMapVO(final long loadBalancerId, final long gslbLoadBalancerId, final long weight) {
        this.loadBalancerId = loadBalancerId;
        this.gslbLoadBalancerId = gslbLoadBalancerId;
        this.revoke = false;
        this.weight = weight;
    }

    @Override
    public long getId() {
        return id;
    }

    public long getLoadBalancerId() {
        return loadBalancerId;
    }

    public void setLoadBalancerId(final long loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public long getGslbLoadBalancerId() {
        return gslbLoadBalancerId;
    }

    public void setGslbLoadBalancerId(final long gslbLoadBalancerId) {
        this.gslbLoadBalancerId = gslbLoadBalancerId;
    }

    public boolean isRevoke() {
        return revoke;
    }

    public void setRevoke(final boolean revoke) {
        this.revoke = revoke;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(final long weight) {
        this.weight = weight;
    }
}
