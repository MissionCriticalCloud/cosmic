//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.PortForwardingRuleTO;

import java.util.List;

/**
 *
 */
public class ConfigurePortForwardingRulesOnLogicalRouterCommand extends Command {

    private String logicalRouterUuid;
    private List<PortForwardingRuleTO> rules;

    public ConfigurePortForwardingRulesOnLogicalRouterCommand(final String logicalRouterUuid, final List<PortForwardingRuleTO> rules) {
        this.logicalRouterUuid = logicalRouterUuid;
        this.rules = rules;
    }

    public String getLogicalRouterUuid() {
        return logicalRouterUuid;
    }

    public void setLogicalRouterUuid(final String logicalRouterUuid) {
        this.logicalRouterUuid = logicalRouterUuid;
    }

    public List<PortForwardingRuleTO> getRules() {
        return rules;
    }

    public void setRules(final List<PortForwardingRuleTO> rules) {
        this.rules = rules;
    }

    /* (non-Javadoc)
     * @see com.cloud.agent.api.Command#executeInSequence()
     */
    @Override
    public boolean executeInSequence() {
        return false;
    }
}
