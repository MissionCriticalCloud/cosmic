//

//

package com.cloud.agent.api;

import com.cloud.host.Host;

public class PingCommand extends Command {
    Host.Type hostType;
    long hostId;

    protected PingCommand() {
    }

    public PingCommand(final Host.Type type, final long id) {
        hostType = type;
        hostId = id;
    }

    public Host.Type getHostType() {
        return hostType;
    }

    public long getHostId() {
        return hostId;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (hostType != null ? hostType.hashCode() : 0);
        result = 31 * result + (int) (hostId ^ (hostId >>> 32));
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PingCommand)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final PingCommand that = (PingCommand) o;

        if (hostId != that.hostId) {
            return false;
        }
        if (hostType != that.hostType) {
            return false;
        }

        return true;
    }
}
