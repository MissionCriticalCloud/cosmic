package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.network.vpc.Vpc;

public class SetupVRCommand extends NetworkElementCommand {
    private String sourceNatList;
    private String vpcName;

    protected SetupVRCommand() {
    }

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
