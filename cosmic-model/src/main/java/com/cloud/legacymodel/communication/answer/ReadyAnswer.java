package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.ReadyCommand;

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
