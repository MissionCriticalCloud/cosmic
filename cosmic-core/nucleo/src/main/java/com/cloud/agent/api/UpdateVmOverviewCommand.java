package com.cloud.agent.api;

import com.cloud.legacymodel.communication.command.NetworkElementCommand;
import com.cloud.legacymodel.to.VMOverviewTO;

public class UpdateVmOverviewCommand extends NetworkElementCommand {
    private VMOverviewTO vmOverview;

    public UpdateVmOverviewCommand() {
    }

    public UpdateVmOverviewCommand(final VMOverviewTO vmOverview) {
        this.vmOverview = vmOverview;
    }

    public VMOverviewTO getVmOverview() {
        return vmOverview;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
