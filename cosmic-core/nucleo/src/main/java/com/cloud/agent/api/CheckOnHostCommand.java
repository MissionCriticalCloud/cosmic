//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.HostTO;
import com.cloud.host.Host;

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
