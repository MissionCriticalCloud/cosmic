package com.cloud.host;

public interface HostStats {

    // host related stats
    public double getCpuUtilization();

    public double getNetworkWriteKBs();

    public double getTotalMemoryKBs();

    public double getFreeMemoryKBs();

    public double getNetworkReadKBs();

    public String getEntityType();

    public double getUsedMemory();

    public HostStats getHostStats();

    // public double getAverageLoad();
    // public double getXapiMemoryUsageKBs();
}
