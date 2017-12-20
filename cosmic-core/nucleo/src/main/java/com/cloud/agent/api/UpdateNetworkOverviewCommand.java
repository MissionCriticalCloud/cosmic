package com.cloud.agent.api;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.to.overviews.NetworkOverviewTO;

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
