package com.cloud.agent.api;

public class UnsupportedAnswer extends Answer {
    protected UnsupportedAnswer() {
        super();
    }

    public UnsupportedAnswer(final Command cmd, final String details) {
        super(cmd, false, details);
    }
}
