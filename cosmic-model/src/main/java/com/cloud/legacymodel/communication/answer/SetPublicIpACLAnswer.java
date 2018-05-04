package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.SetPublicIpACLCommand;

public class SetPublicIpACLAnswer extends Answer {
    String[] results;

    protected SetPublicIpACLAnswer() {
    }

    public SetPublicIpACLAnswer(final SetPublicIpACLCommand cmd, final boolean success, final String[] results) {
        super(cmd, success, null);
        assert (cmd.getRules().length == results.length) : "ACLs and their results should be the same length";
        this.results = results;
    }

    public String[] getResults() {
        return results;
    }
}
