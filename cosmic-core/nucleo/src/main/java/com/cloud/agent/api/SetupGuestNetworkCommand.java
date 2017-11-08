package com.cloud.agent.api;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.to.NicTO;

public class SetupGuestNetworkCommand extends NetworkElementCommand {
    private NicTO nic;

    protected SetupGuestNetworkCommand() {
    }

    public SetupGuestNetworkCommand(final NicTO nic) {
        this.nic = nic;
    }

    public NicTO getNic() {
        return nic;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
