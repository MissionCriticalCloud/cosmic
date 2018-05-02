package com.cloud.agent.api.routing;

import com.cloud.legacymodel.communication.answer.Answer;

public class SetNetworkACLAnswer extends Answer {
    String[] results;

    protected SetNetworkACLAnswer() {
    }

    public SetNetworkACLAnswer(final SetNetworkACLCommand cmd, final boolean success, final String[] results) {
        super(cmd, success, null);
        assert (cmd.getRules().length == results.length) : "ACLs and their results should be the same length";
        this.results = results;
    }

    public String[] getResults() {
        return results;
    }
}
