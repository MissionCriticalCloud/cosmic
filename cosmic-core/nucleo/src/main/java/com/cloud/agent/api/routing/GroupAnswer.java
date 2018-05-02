package com.cloud.agent.api.routing;

import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.Command;

public class GroupAnswer extends Answer {
    String[] results;

    protected GroupAnswer() {
        super();
    }

    public GroupAnswer(final Command cmd, final boolean success, final int rulesCount, final String[] results) {
        super(cmd, success, null);

        assert (rulesCount == results.length) : "Results' count should match requests' count!";
        this.results = results;
    }

    public String[] getResults() {
        return results;
    }
}
