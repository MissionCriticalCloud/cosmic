//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.Answer;

public class SetStaticNatRulesAnswer extends Answer {
    String[] results;

    protected SetStaticNatRulesAnswer() {
        super();
    }

    public SetStaticNatRulesAnswer(final SetStaticNatRulesCommand cmd, final String[] results, final boolean success) {
        super(cmd, success, null);

        assert (cmd.getRules().length == results.length) : "Shouldn't the results match the commands?";
        this.results = results;
    }

    String[] getResults() {
        return results;
    }
}
