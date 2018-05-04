package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.ChangeAgentCommand;

public class ChangeAgentAnswer extends Answer {
    protected ChangeAgentAnswer() {
    }

    public ChangeAgentAnswer(final ChangeAgentCommand cmd, final boolean result) {
        super(cmd, result, null);
    }
}
