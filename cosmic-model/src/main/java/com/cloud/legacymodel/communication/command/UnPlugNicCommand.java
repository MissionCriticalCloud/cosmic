package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.NicTO;

public class UnPlugNicCommand extends Command {
    NicTO nic;
    String instanceName;

    protected UnPlugNicCommand() {
    }

    public UnPlugNicCommand(final NicTO nic, final String instanceName) {
        this.nic = nic;
        this.instanceName = instanceName;
    }

    public NicTO getNic() {
        return nic;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getVmName() {
        return instanceName;
    }
}
