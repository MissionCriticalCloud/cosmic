package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;

public class SetupAnswer extends Answer {
    // indicate if agent reconnect is needed after setup command
    private boolean _reconnect;

    public SetupAnswer() {
    }

    public SetupAnswer(final SetupCommand cmd, final boolean reconnect) {
        super(cmd, true, null);
        _reconnect = reconnect;
    }

    public SetupAnswer(final SetupCommand cmd, final String details) {
        super(cmd, false, details);
        _reconnect = true;
    }

    public boolean needReconnect() {
        return _reconnect;
    }
}
