package com.cloud.legacymodel.network.rules;

import java.util.List;

public class LoadBalancerRules extends ConfigBase {
    List<LoadBalancerRule> rules;

    public LoadBalancerRules() {
        super(ConfigBase.LOAD_BALANCER);
    }

    public LoadBalancerRules(final List<LoadBalancerRule> rules) {
        super(ConfigBase.LOAD_BALANCER);
        this.rules = rules;
    }

    public List<LoadBalancerRule> getRules() {
        return this.rules;
    }

    public void setRules(final List<LoadBalancerRule> rules) {
        this.rules = rules;
    }
}
