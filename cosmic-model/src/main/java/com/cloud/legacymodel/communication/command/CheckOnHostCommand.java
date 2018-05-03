package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.to.HostTO;

public class CheckOnHostCommand extends Command {
    HostTO host;

    protected CheckOnHostCommand() {
    }

    public CheckOnHostCommand(final Host host) {
        this.host = new HostTO(host);
        setWait(20);
    }

    public HostTO getHost() {
        return host;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
