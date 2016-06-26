//

//

package com.cloud.agent.api;

import com.cloud.agent.api.to.NicTO;

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
