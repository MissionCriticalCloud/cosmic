package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.ScaleVmCommand;

public class ScaleVmAnswer extends Answer {

    protected ScaleVmAnswer() {
    }

    public ScaleVmAnswer(final ScaleVmCommand cmd, final boolean result, final String detail) {
        super(cmd, result, detail);
    }
}
