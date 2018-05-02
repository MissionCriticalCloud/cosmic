package com.cloud.agent.api;

import com.cloud.legacymodel.communication.LogLevel;
import com.cloud.legacymodel.communication.LogLevel.Level;
import com.cloud.legacymodel.communication.answer.Answer;

import java.util.HashMap;

@LogLevel(Level.Trace)
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
