package com.cloud.agent.api;

import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.network.vpc.Vpc;

public class SetupVRCommand extends NetworkElementCommand {
    private String sourceNatList;
    private String vpcName;

    protected SetupVRCommand() {}

    public SetupVRCommand(final Vpc vpc) {
        this.sourceNatList = vpc.getSourceNatList();
        this.vpcName = vpc.getName();
    }

    public String getSourceNatList() {
        return sourceNatList;
    }

    public String getVpcName() {
        return vpcName;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
