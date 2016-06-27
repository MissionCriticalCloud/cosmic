//

//

package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;

import java.util.List;

@LogLevel(Log4jLevel.Trace)
public class GetVmDiskStatsCommand extends Command {
    List<String> vmNames;
    String hostGuid;
    String hostName;

    protected GetVmDiskStatsCommand() {
    }

    public GetVmDiskStatsCommand(final List<String> vmNames, final String hostGuid, final String hostName) {
        this.vmNames = vmNames;
        this.hostGuid = hostGuid;
        this.hostName = hostName;
    }

    public List<String> getVmNames() {
        return vmNames;
    }

    public String getHostGuid() {
        return this.hostGuid;
    }

    public String getHostName() {
        return this.hostName;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
