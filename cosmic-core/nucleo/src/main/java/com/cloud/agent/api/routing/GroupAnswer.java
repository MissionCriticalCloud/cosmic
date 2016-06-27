//

//

package com.cloud.agent.api.routing;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.Command;

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
