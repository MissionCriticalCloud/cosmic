package com.cloud.legacymodel.dc;

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
