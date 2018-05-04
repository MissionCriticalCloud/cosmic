package com.cloud.legacymodel.vm;

public interface VmStats {
    double getCPUUtilization();

    double getNetworkReadKBs();

    double getNetworkWriteKBs();

    double getDiskReadIOs();

    double getDiskWriteIOs();

    double getDiskReadKBs();

    double getDiskWriteKBs();
}
