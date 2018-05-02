package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;

public class ScaleVmAnswer extends Answer {

    protected ScaleVmAnswer() {
    }

    public ScaleVmAnswer(final ScaleVmCommand cmd, final boolean result, final String detail) {
        super(cmd, result, detail);
    }
}
