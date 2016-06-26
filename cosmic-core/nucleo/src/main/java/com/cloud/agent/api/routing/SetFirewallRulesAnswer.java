//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.Answer;

public class SetFirewallRulesAnswer extends Answer {
    String[] results;

    protected SetFirewallRulesAnswer() {
    }

    public SetFirewallRulesAnswer(final SetFirewallRulesCommand cmd, final boolean success, final String[] results) {
        super(cmd, success, null);
        assert (cmd.getRules().length == results.length) : "rules and their results should be the same length don't you think?";
        this.results = results;
    }

    public String[] getResults() {
        return results;
    }
}
