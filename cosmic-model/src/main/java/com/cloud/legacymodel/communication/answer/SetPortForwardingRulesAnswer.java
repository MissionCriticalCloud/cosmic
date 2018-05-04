package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.SetPortForwardingRulesCommand;

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
