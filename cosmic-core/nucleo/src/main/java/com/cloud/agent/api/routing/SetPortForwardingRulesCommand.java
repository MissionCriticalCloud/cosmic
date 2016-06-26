//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.PortForwardingRuleTO;

import java.util.List;

public class SetPortForwardingRulesCommand extends NetworkElementCommand {
    PortForwardingRuleTO[] rules;

    protected SetPortForwardingRulesCommand() {
    }

    public SetPortForwardingRulesCommand(final List<? extends PortForwardingRuleTO> pfRules) {
        rules = new PortForwardingRuleTO[pfRules.size()];
        int i = 0;
        for (final PortForwardingRuleTO rule : pfRules) {
            rules[i++] = rule;
        }
    }

    public PortForwardingRuleTO[] getRules() {
        return rules;
    }

    @Override
    public int getAnswersCount() {
        return rules.length;
    }
}

