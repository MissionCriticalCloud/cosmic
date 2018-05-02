package com.cloud.agent.api;

import com.cloud.legacymodel.communication.answer.Answer;

public class RebootAnswer extends Answer {
    Integer vncPort;

    protected RebootAnswer() {
    }

    public RebootAnswer(final RebootCommand cmd, final String details, final Integer vncport) {
        super(cmd, true, details);
        this.vncPort = vncport;
    }

    public RebootAnswer(final RebootCommand cmd, final String details, final boolean success) {
        super(cmd, success, details);
        this.vncPort = null;
    }

    public RebootAnswer(final RebootCommand cmd, final Exception e) {
        super(cmd, false, e.getMessage());
    }

    public Integer getVncPort() {
        return vncPort;
    }
}
