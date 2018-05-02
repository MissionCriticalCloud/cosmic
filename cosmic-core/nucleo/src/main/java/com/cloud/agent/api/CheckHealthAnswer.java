package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;

public class CheckHealthAnswer extends Answer {

    public CheckHealthAnswer() {
    }

    public CheckHealthAnswer(final CheckHealthCommand cmd, final boolean alive) {
        super(cmd, alive, "resource is " + (alive ? "alive" : "not alive"));
    }
}
