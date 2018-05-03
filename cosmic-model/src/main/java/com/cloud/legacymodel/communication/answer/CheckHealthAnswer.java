package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.CheckHealthCommand;

public class CheckHealthAnswer extends Answer {

    public CheckHealthAnswer() {
    }

    public CheckHealthAnswer(final CheckHealthCommand cmd, final boolean alive) {
        super(cmd, alive, "resource is " + (alive ? "alive" : "not alive"));
    }
}
