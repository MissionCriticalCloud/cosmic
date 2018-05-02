package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;

public class PingAnswer extends Answer {
    private PingCommand _command = null;

    protected PingAnswer() {
    }

    public PingAnswer(final PingCommand cmd) {
        super(cmd);
        _command = cmd;
    }

    public PingCommand getCommand() {
        return _command;
    }
}
