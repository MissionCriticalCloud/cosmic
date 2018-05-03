package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.PlugNicCommand;

public class PlugNicAnswer extends Answer {
    public PlugNicAnswer() {
    }

    public PlugNicAnswer(final PlugNicCommand cmd, final boolean success, final String result) {
        super(cmd, success, result);
    }
}
