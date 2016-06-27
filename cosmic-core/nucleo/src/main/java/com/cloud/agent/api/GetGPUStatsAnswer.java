//

//

package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;

import java.util.HashMap;

@LogLevel(Log4jLevel.Trace)
public class GetGPUStatsAnswer extends Answer {

    private HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails;

    public GetGPUStatsAnswer(final GetGPUStatsCommand cmd, final HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails) {
        super(cmd);
        this.groupDetails = groupDetails;
    }

    public GetGPUStatsAnswer(final GetGPUStatsCommand cmd, final boolean success, final String details) {
        super(cmd, success, details);
    }

    public HashMap<String, HashMap<String, VgpuTypesInfo>> getGroupDetails() {
        return groupDetails;
    }
}
