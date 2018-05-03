package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.command.CheckOnHostCommand;

public class CheckOnHostAnswer extends Answer {
    boolean determined;
    boolean alive;

    protected CheckOnHostAnswer() {
    }

    public CheckOnHostAnswer(final CheckOnHostCommand cmd, final Boolean alive, final String details) {
        super(cmd, true, details);
        if (alive == null) {
            determined = false;
        } else {
            determined = true;
            this.alive = alive;
        }
    }

    public CheckOnHostAnswer(final CheckOnHostCommand cmd, final String details) {
        super(cmd, false, details);
    }

    public boolean isDetermined() {
        return determined;
    }

    public boolean isAlive() {
        return alive;
    }
}
