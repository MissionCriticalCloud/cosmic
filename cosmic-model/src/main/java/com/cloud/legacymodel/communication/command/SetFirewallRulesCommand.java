package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.FirewallRuleTO;

import java.util.List;

/**
 * AccessDetails allow different components to put in information about
 * how to access the components inside the command.
 */
public class SetFirewallRulesCommand extends NetworkElementCommand {
    FirewallRuleTO[] rules;

    protected SetFirewallRulesCommand() {
    }

    public SetFirewallRulesCommand(final List<FirewallRuleTO> rules) {
        this.rules = rules.toArray(new FirewallRuleTO[rules.size()]);
    }

    public FirewallRuleTO[] getRules() {
        return rules;
    }

    @Override
    public int getAnswersCount() {
        return rules.length;
    }
}
