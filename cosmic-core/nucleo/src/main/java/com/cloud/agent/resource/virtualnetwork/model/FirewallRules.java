//

//

package com.cloud.agent.resource.virtualnetwork.model;

public class FirewallRules extends ConfigBase {
    FirewallRule[] rules;

    public FirewallRules() {
        super(ConfigBase.FIREWALL_RULES);
    }

    public FirewallRules(final FirewallRule[] rules) {
        super(ConfigBase.FIREWALL_RULES);
        this.rules = rules;
    }

    public FirewallRule[] getRules() {
        return rules;
    }

    public void setRules(final FirewallRule[] rules) {
        this.rules = rules;
    }
}
