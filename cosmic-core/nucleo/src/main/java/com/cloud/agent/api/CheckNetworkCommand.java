//

//

package com.cloud.agent.api;

import com.cloud.network.PhysicalNetworkSetupInfo;

import java.util.List;

public class CheckNetworkCommand extends Command {

    List<PhysicalNetworkSetupInfo> networkInfoList;

    public CheckNetworkCommand(final List<PhysicalNetworkSetupInfo> networkInfoList) {
        this.networkInfoList = networkInfoList;
    }

    protected CheckNetworkCommand() {
    }

    public List<PhysicalNetworkSetupInfo> getPhysicalNetworkInfoList() {
        return networkInfoList;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }
}
