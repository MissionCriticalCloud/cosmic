package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.communication.LogLevel;
import com.cloud.legacymodel.communication.LogLevel.Level;

@LogLevel(Level.Trace)
public class GetGPUStatsCommand extends Command {
    String hostGuid;
    String hostName;

    protected GetGPUStatsCommand() {
    }

    public GetGPUStatsCommand(final String hostGuid, final String hostName) {
        this.hostGuid = hostGuid;
        this.hostName = hostName;
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
