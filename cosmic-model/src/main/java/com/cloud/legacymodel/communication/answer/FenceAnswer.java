package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.FenceCommand;

public class FenceAnswer extends Answer {
    public FenceAnswer() {
        super();
    }

    public FenceAnswer(final FenceCommand cmd) {
        super(cmd, true, null);
    }

    public FenceAnswer(final FenceCommand cmd, final String details) {
        super(cmd, true, details);
    }

    public FenceAnswer(final FenceCommand cmd, final boolean result, final String details) {
        super(cmd, result, details);
    }
}
