package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.UnPlugNicCommand;

public class UnPlugNicAnswer extends Answer {
    public UnPlugNicAnswer() {
    }

    public UnPlugNicAnswer(final UnPlugNicCommand cmd, final boolean success, final String result) {
        super(cmd, success, result);
    }
}
