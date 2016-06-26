//

//

package com.cloud.agent.api;

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
        super(cmd, e);
    }

    public Integer getVncPort() {
        return vncPort;
    }
}
