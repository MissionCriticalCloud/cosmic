package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;

public class ReadyAnswer extends Answer {
    protected ReadyAnswer() {
    }

    public ReadyAnswer(final ReadyCommand cmd) {
        super(cmd, true, null);
    }

    public ReadyAnswer(final ReadyCommand cmd, final String details) {
        super(cmd, false, details);
    }
}
