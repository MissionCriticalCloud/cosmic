package com.cloud.legacymodel.communication.answer;

import com.cloud.legacymodel.communication.LogLevel;
import com.cloud.legacymodel.communication.LogLevel.Level;
import com.cloud.legacymodel.communication.command.GetVmDiskStatsCommand;
import com.cloud.legacymodel.storage.VmDiskStatsEntry;

import java.util.HashMap;
import java.util.List;

@LogLevel(Level.Trace)
public class GetVmDiskStatsAnswer extends Answer {

    String hostName;
    HashMap<String, List<VmDiskStatsEntry>> vmDiskStatsMap;

    public GetVmDiskStatsAnswer(final GetVmDiskStatsCommand cmd, final String details, final String hostName, final HashMap<String, List<VmDiskStatsEntry>> vmDiskStatsMap) {
        super(cmd, true, details);
        this.hostName = hostName;
        this.vmDiskStatsMap = vmDiskStatsMap;
    }

    protected GetVmDiskStatsAnswer() {
        //no-args constructor for json serialization-deserialization
    }

    public String getHostName() {
        return hostName;
    }

    public HashMap<String, List<VmDiskStatsEntry>> getVmDiskStatsMap() {
        return vmDiskStatsMap;
    }
}
