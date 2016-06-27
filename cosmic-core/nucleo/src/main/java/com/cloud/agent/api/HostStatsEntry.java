//

//

package com.cloud.agent.api;

import com.cloud.host.HostStats;

public class HostStatsEntry implements HostStats {

    long hostId;
    String entityType;
    double cpuUtilization;
    double networkReadKBs;
    double networkWriteKBs;
    double totalMemoryKBs;
    double freeMemoryKBs;

    public HostStatsEntry() {
    }

    public HostStatsEntry(final long hostId, final double cpuUtilization, final double networkReadKBs, final double networkWriteKBs, final String entityType, final double
            totalMemoryKBs,
                          final double freeMemoryKBs, final double xapiMemoryUsageKBs, final double averageLoad) {
        this.hostId = hostId;
        this.entityType = entityType;
        this.cpuUtilization = cpuUtilization;
        this.networkReadKBs = networkReadKBs;
        this.networkWriteKBs = networkWriteKBs;
        this.totalMemoryKBs = totalMemoryKBs;
        this.freeMemoryKBs = freeMemoryKBs;
    }

    @Override
    public double getCpuUtilization() {
        return this.cpuUtilization;
    }

    @Override
    public double getNetworkWriteKBs() {
        return networkWriteKBs;
    }

    public void setNetworkWriteKBs(final double networkWriteKBs) {
        this.networkWriteKBs = networkWriteKBs;
    }

    @Override
    public double getTotalMemoryKBs() {
        return this.totalMemoryKBs;
    }

    public void setTotalMemoryKBs(final double totalMemoryKBs) {
        this.totalMemoryKBs = totalMemoryKBs;
    }

    @Override
    public double getFreeMemoryKBs() {
        return this.freeMemoryKBs;
    }

    @Override
    public double getNetworkReadKBs() {
        return networkReadKBs;
    }

    public void setNetworkReadKBs(final double networkReadKBs) {
        this.networkReadKBs = networkReadKBs;
    }

    @Override
    public String getEntityType() {
        return this.entityType;
    }

    public void setEntityType(final String entityType) {
        this.entityType = entityType;
    }

    @Override
    public double getUsedMemory() {
        return (totalMemoryKBs - freeMemoryKBs) * 1024;
    }

    @Override
    public HostStats getHostStats() {
        return this;
    }

    public void setFreeMemoryKBs(final double freeMemoryKBs) {
        this.freeMemoryKBs = freeMemoryKBs;
    }

    public void setCpuUtilization(final double cpuUtilization) {
        this.cpuUtilization = cpuUtilization;
    }

    public void setHostId(final long hostId) {
        this.hostId = hostId;
    }
}
