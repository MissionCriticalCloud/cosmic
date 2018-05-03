package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.to.NicTO;

import java.util.List;

public class GetVmConfigCommand extends Command {
    String vmName;
    List<NicTO> nics;

    protected GetVmConfigCommand() {
    }

    public GetVmConfigCommand(final String vmName) {
        this.vmName = vmName;
    }

    public String getVmName() {
        return vmName;
    }

    public void setNics(final List<NicTO> nics) {
        this.nics = nics;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
