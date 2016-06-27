package com.cloud.vm;

public interface VmStats {
    // vm related stats
    public double getCPUUtilization();

    public double getNetworkReadKBs();

    public double getNetworkWriteKBs();

    public double getDiskReadIOs();

    public double getDiskWriteIOs();

    public double getDiskReadKBs();

    public double getDiskWriteKBs();
}
