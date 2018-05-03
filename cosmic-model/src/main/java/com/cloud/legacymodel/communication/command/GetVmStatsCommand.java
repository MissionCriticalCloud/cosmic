package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.communication.LogLevel;
import com.cloud.legacymodel.communication.LogLevel.Level;

import java.util.List;

@LogLevel(Level.Trace)
public class GetVmStatsCommand extends Command {
    List<String> vmNames;
    String hostGuid;
    String hostName;

    protected GetVmStatsCommand() {
    }

    public GetVmStatsCommand(final List<String> vmNames, final String hostGuid, final String hostName) {
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
