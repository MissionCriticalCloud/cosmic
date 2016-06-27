//

//

package com.cloud.agent.resource.virtualnetwork.model;

import java.util.List;

public class StaticNatRules extends ConfigBase {
    private List<StaticNatRule> rules;

    public StaticNatRules() {
        super(ConfigBase.STATICNAT_RULES);
    }

    public StaticNatRules(final List<StaticNatRule> rules) {
        super(ConfigBase.STATICNAT_RULES);
        this.rules = rules;
    }

    public List<StaticNatRule> getRules() {
        return rules;
    }

    public void setRules(final List<StaticNatRule> rules) {
        this.rules = rules;
    }
}
