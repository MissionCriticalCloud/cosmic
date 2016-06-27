//

//

package com.cloud.agent.api;

import com.cloud.vm.VmStats;

public class VmStatsEntry implements VmStats {

    double cpuUtilization;
    double networkReadKBs;
    double networkWriteKBs;
    double diskReadIOs;
    double diskWriteIOs;
    double diskReadKBs;
    double diskWriteKBs;
    int numCPUs;
    String entityType;

    public VmStatsEntry() {
    }

    public VmStatsEntry(final double cpuUtilization, final double networkReadKBs, final double networkWriteKBs, final int numCPUs, final String entityType) {
        this.cpuUtilization = cpuUtilization;
        this.networkReadKBs = networkReadKBs;
        this.networkWriteKBs = networkWriteKBs;
        this.numCPUs = numCPUs;
        this.entityType = entityType;
    }

    public VmStatsEntry(final double cpuUtilization, final double networkReadKBs, final double networkWriteKBs, final double diskReadKBs, final double diskWriteKBs, final int
            numCPUs, final String entityType) {
        this.cpuUtilization = cpuUtilization;
        this.networkReadKBs = networkReadKBs;
        this.networkWriteKBs = networkWriteKBs;
        this.diskReadKBs = diskReadKBs;
        this.diskWriteKBs = diskWriteKBs;
        this.numCPUs = numCPUs;
        this.entityType = entityType;
    }

    @Override
    public double getCPUUtilization() {
        return cpuUtilization;
    }

    public void setCPUUtilization(final double cpuUtilization) {
        this.cpuUtilization = cpuUtilization;
    }

    @Override
    public double getNetworkReadKBs() {
        return networkReadKBs;
    }

    public void setNetworkReadKBs(final double networkReadKBs) {
        this.networkReadKBs = networkReadKBs;
    }

    @Override
    public double getNetworkWriteKBs() {
        return networkWriteKBs;
    }

    public void setNetworkWriteKBs(final double networkWriteKBs) {
        this.networkWriteKBs = networkWriteKBs;
    }

    @Override
    public double getDiskReadIOs() {
        return diskReadIOs;
    }

    public void setDiskReadIOs(final double diskReadIOs) {
        this.diskReadIOs = diskReadIOs;
    }

    @Override
    public double getDiskWriteIOs() {
        return diskWriteIOs;
    }

    public void setDiskWriteIOs(final double diskWriteIOs) {
        this.diskWriteIOs = diskWriteIOs;
    }

    @Override
    public double getDiskReadKBs() {
        return diskReadKBs;
    }

    public void setDiskReadKBs(final double diskReadKBs) {
        this.diskReadKBs = diskReadKBs;
    }

    @Override
    public double getDiskWriteKBs() {
        return diskWriteKBs;
    }

    public void setDiskWriteKBs(final double diskWriteKBs) {
        this.diskWriteKBs = diskWriteKBs;
    }

    public int getNumCPUs() {
        return numCPUs;
    }

    public void setNumCPUs(final int numCPUs) {
        this.numCPUs = numCPUs;
    }

    public String getEntityType() {
        return this.entityType;
    }

    public void setEntityType(final String entityType) {
        this.entityType = entityType;
    }
}
