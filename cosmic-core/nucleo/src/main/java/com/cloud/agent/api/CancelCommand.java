//

//

package com.cloud.agent.api;

public class CancelCommand extends Command {
    protected long sequence;
    protected String reason;

    protected CancelCommand() {
    }

    public CancelCommand(final long sequence, final String reason) {
        this.sequence = sequence;
        this.reason = reason;
    }

    public long getSequence() {
        return sequence;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
