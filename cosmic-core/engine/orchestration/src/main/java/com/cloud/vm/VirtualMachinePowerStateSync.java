package com.cloud.vm;

import com.cloud.agent.api.HostVmStateReportEntry;

import java.util.Map;

public interface VirtualMachinePowerStateSync {

    void resetHostSyncState(long hostId);

    void processHostVmStateReport(long hostId, Map<String, HostVmStateReportEntry> report);

    // to adapt legacy ping report
    void processHostVmStatePingReport(long hostId, Map<String, HostVmStateReportEntry> report);

    Map<Long, VirtualMachine.PowerState> convertVmStateReport(Map<String, HostVmStateReportEntry> states);
}
