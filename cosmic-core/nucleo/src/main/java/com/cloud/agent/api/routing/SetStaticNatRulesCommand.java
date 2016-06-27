//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.to.StaticNatRuleTO;

import java.util.List;

public class SetStaticNatRulesCommand extends NetworkElementCommand {

    StaticNatRuleTO[] rules;
    Long vpcId;

    protected SetStaticNatRulesCommand() {
    }

    public SetStaticNatRulesCommand(final List<? extends StaticNatRuleTO> staticNatRules, final Long vpcId) {
        rules = new StaticNatRuleTO[staticNatRules.size()];
        int i = 0;
        for (final StaticNatRuleTO rule : staticNatRules) {
            rules[i++] = rule;
        }
        this.vpcId = vpcId;
    }

    public StaticNatRuleTO[] getRules() {
        return rules;
    }

    public Long getVpcId() {
        return vpcId;
    }

    @Override
    public int getAnswersCount() {
        return rules.length;
    }
}
