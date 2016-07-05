package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Level;

import java.util.HashMap;

@LogLevel(Level.Trace)
public class GetVmStatsAnswer extends Answer {

    HashMap<String, VmStatsEntry> vmStatsMap;

    public GetVmStatsAnswer(final GetVmStatsCommand cmd, final HashMap<String, VmStatsEntry> vmStatsMap) {
        super(cmd);
        this.vmStatsMap = vmStatsMap;
    }

    protected GetVmStatsAnswer() {
        //no-args constructor for json serialization-deserialization
    }

    public HashMap<String, VmStatsEntry> getVmStatsMap() {
        return vmStatsMap;
    }
}
