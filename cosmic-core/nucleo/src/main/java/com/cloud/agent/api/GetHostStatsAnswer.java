//

//

package com.cloud.agent.api;

import com.cloud.agent.api.LogLevel.Log4jLevel;
import com.cloud.host.HostStats;

@LogLevel(Log4jLevel.Trace)
public class GetHostStatsAnswer extends Answer implements HostStats {

    HostStatsEntry hostStats;

    protected GetHostStatsAnswer() {
        hostStats = new HostStatsEntry();
    }

    public GetHostStatsAnswer(final GetHostStatsCommand cmd, final HostStatsEntry hostStatistics) {
        super(cmd);
        this.hostStats = hostStatistics;
    }

    public GetHostStatsAnswer(final GetHostStatsCommand cmd, final double cpuUtilization, final double freeMemoryKBs, final double totalMemoryKBs, final double networkReadKBs,
                              final double networkWriteKBs,
                              final String entityType) {
        super(cmd);
        hostStats = new HostStatsEntry();

        hostStats.setCpuUtilization(cpuUtilization);
        hostStats.setFreeMemoryKBs(freeMemoryKBs);
        hostStats.setTotalMemoryKBs(totalMemoryKBs);
        hostStats.setNetworkReadKBs(networkReadKBs);
        hostStats.setNetworkWriteKBs(networkWriteKBs);
        hostStats.setEntityType(entityType);
    }

    @Override
    public double getCpuUtilization() {
        return hostStats.getCpuUtilization();
    }

    @Override
    public double getNetworkWriteKBs() {
        return hostStats.getNetworkWriteKBs();
    }

    @Override
    public double getTotalMemoryKBs() {
        return hostStats.getTotalMemoryKBs();
    }

    @Override
    public double getFreeMemoryKBs() {
        return hostStats.getFreeMemoryKBs();
    }

    @Override
    public double getNetworkReadKBs() {
        return hostStats.getNetworkReadKBs();
    }

    @Override
    public String getEntityType() {
        return hostStats.getEntityType();
    }

    @Override
    public double getUsedMemory() {
        return hostStats.getUsedMemory();
    }

    @Override
    public HostStats getHostStats() {
        return hostStats;
    }
}
