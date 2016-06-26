//

//

package com.cloud.agent.api;

import java.util.UUID;

/**
 * This command will tell the hypervisor to cleanup any resources dedicated for
 * this particular nic. Orginally implemented to cleanup dedicated portgroups
 * from a vmware standard switch
 */
public class UnregisterNicCommand extends Command {
    private final String vmName;
    private final String trafficLabel;
    private final UUID nicUuid;

    public UnregisterNicCommand(final String vmName, final String trafficLabel, final UUID nicUuid) {
        this.nicUuid = nicUuid;
        this.vmName = vmName;
        this.trafficLabel = trafficLabel;
    }

    public UUID getNicUuid() {
        return nicUuid;
    }

    public String getVmName() {
        return vmName;
    }

    public String getTrafficLabel() {
        return trafficLabel;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
