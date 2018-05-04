package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.DestroyCommand;

public class DestroyAnswer extends Answer {
    public DestroyAnswer(final DestroyCommand cmd, final boolean result, final String details) {
        super(cmd, result, details);
    }

    // Constructor for gson.
    protected DestroyAnswer() {
        super();
    }
}
