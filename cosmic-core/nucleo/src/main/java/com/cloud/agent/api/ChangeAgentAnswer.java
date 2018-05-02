package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;

public class ChangeAgentAnswer extends Answer {
    protected ChangeAgentAnswer() {
    }

    public ChangeAgentAnswer(final ChangeAgentCommand cmd, final boolean result) {
        super(cmd, result, null);
    }
}
