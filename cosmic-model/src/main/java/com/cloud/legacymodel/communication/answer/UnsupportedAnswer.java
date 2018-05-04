package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.Command;

public class UnsupportedAnswer extends Answer {
    protected UnsupportedAnswer() {
        super();
    }

    public UnsupportedAnswer(final Command cmd, final String details) {
        super(cmd, false, details);
    }
}
