//

//

package com.cloud.agent.api;

import com.cloud.host.Status.Event;

public class TransferAgentCommand extends Command {
    protected long agentId;
    protected long futureOwner;
    protected long currentOwner;
    Event event;

    protected TransferAgentCommand() {
    }

    public TransferAgentCommand(final long agentId, final long currentOwner, final long futureOwner, final Event event) {
        this.agentId = agentId;
        this.currentOwner = currentOwner;
        this.futureOwner = futureOwner;
        this.event = event;
    }

    public long getAgentId() {
        return agentId;
    }

    public long getFutureOwner() {
        return futureOwner;
    }

    public Event getEvent() {
        return event;
    }

    public long getCurrentOwner() {
        return currentOwner;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
