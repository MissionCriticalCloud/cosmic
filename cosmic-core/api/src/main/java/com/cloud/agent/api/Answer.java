package com.cloud.agent.api;

import com.cloud.utils.exception.ExceptionUtil;

public class Answer extends Command {
    protected boolean result;
    protected String details;

    protected Answer() {
        this(null);
    }

    public Answer(final Command command) {
        this(command, true, null);
    }

    public Answer(final Command command, final boolean success, final String details) {
        result = success;
        this.details = details;
    }

    public Answer(final Command command, final Exception e) {
        this(command, false, ExceptionUtil.toString(e));
    }

    public static UnsupportedAnswer createUnsupportedCommandAnswer(final Command cmd) {
        return new UnsupportedAnswer(cmd, "Unsupported command issued: " + cmd.toString() + ".  Are you sure you got the right type of server?");
    }

    public static UnsupportedAnswer createUnsupportedVersionAnswer(final Command cmd) {
        return new UnsupportedAnswer(cmd, "Unsuppored Version.");
    }

    public boolean getResult() {
        return result;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    @Override
    public int hashCode() {
        int result1 = super.hashCode();
        result1 = 31 * result1 + (result ? 1 : 0);
        result1 = 31 * result1 + (details != null ? details.hashCode() : 0);
        return result1;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Answer)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final Answer answer = (Answer) o;

        if (result != answer.result) {
            return false;
        }
        if (details != null ? !details.equals(answer.details) : answer.details != null) {
            return false;
        }

        return true;
    }
}
