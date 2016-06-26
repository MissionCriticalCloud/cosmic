//

//

package com.cloud.agent.api;

public class ChangeAgentAnswer extends Answer {
    protected ChangeAgentAnswer() {
    }

    public ChangeAgentAnswer(final ChangeAgentCommand cmd, final boolean result) {
        super(cmd, result, null);
    }
}
