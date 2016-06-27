//

//

package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;

@LogLevel(Log4jLevel.Trace)
public class GetHostStatsCommand extends Command {
    String hostGuid;
    String hostName;
    long hostId;

    protected GetHostStatsCommand() {
    }

    public GetHostStatsCommand(final String hostGuid, final String hostName, final long hostId) {
        this.hostGuid = hostGuid;
        this.hostName = hostName;
        this.hostId = hostId;
    }

    public String getHostGuid() {
        return this.hostGuid;
    }

    public String getHostName() {
        return this.hostName;
    }

    public long getHostId() {
        return this.hostId;
    }

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
