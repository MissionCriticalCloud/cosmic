package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;

public class UnPlugNicAnswer extends Answer {
    public UnPlugNicAnswer() {
    }

    public UnPlugNicAnswer(final UnPlugNicCommand cmd, final boolean success, final String result) {
        super(cmd, success, result);
    }
}
