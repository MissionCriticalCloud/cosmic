//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.Answer;

public class SetPortForwardingRulesAnswer extends Answer {
    String[] results;

    protected SetPortForwardingRulesAnswer() {
        super();
    }

    public SetPortForwardingRulesAnswer(final SetPortForwardingRulesCommand cmd, final String[] results, final boolean success) {
        super(cmd, success, null);

        assert (cmd.getRules().length == results.length) : "Shouldn't the results match the commands?";
        this.results = results;
    }

    String[] getResults() {
        return results;
    }
}
