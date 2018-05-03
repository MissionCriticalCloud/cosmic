package com.cloud.host;

public interface HostStats {

    double getCpuUtilization();

    double getNetworkWriteKBs();

    double getTotalMemoryKBs();

    double getFreeMemoryKBs();

    double getNetworkReadKBs();

    String getEntityType();

    double getUsedMemory();

    HostStats getHostStats();
}
