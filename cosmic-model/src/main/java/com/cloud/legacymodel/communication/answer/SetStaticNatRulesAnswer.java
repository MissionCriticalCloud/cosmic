package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.SetStaticNatRulesCommand;

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
