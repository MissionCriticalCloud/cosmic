package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.NetworkOverviewTO;

public class UpdateNetworkOverviewCommand extends NetworkElementCommand {
    private NetworkOverviewTO networkOverview;
    private boolean plugNics;

    public UpdateNetworkOverviewCommand() {
    }

    public UpdateNetworkOverviewCommand(final NetworkOverviewTO networkOverview) {
        this.networkOverview = networkOverview;
    }

    public NetworkOverviewTO getNetworkOverview() {
        return networkOverview;
    }

    public boolean isPlugNics() {
        return plugNics;
    }

    public void setPlugNics(final boolean plugNics) {
        this.plugNics = plugNics;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
