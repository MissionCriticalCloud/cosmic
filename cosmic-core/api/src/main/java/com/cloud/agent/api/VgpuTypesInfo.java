package com.cloud.agent.api;

public class VgpuTypesInfo {

    private final String modelName;
    private final String groupName;
    private final Long maxHeads;
    private final Long videoRam;
    private final Long maxResolutionX;
    private final Long maxResolutionY;
    private final Long maxVgpuPerGpu;
    private Long remainingCapacity;
    private Long maxCapacity;

    public VgpuTypesInfo(final String groupName, final String modelName, final Long videoRam, final Long maxHeads, final Long maxResolutionX, final Long maxResolutionY, final
    Long maxVgpuPerGpu,
                         final Long remainingCapacity, final Long maxCapacity) {
        this.groupName = groupName;
        this.modelName = modelName;
        this.videoRam = videoRam;
        this.maxHeads = maxHeads;
        this.maxResolutionX = maxResolutionX;
        this.maxResolutionY = maxResolutionY;
        this.maxVgpuPerGpu = maxVgpuPerGpu;
        this.remainingCapacity = remainingCapacity;
        this.maxCapacity = maxCapacity;
    }

    public String getModelName() {
        return modelName;
    }

    public String getGroupName() {
        return groupName;
    }

    public Long getVideoRam() {
        return videoRam;
    }

    public Long getMaxHeads() {
        return maxHeads;
    }

    public Long getMaxResolutionX() {
        return maxResolutionX;
    }

    public Long getMaxResolutionY() {
        return maxResolutionY;
    }

    public Long getMaxVpuPerGpu() {
        return maxVgpuPerGpu;
    }

    public Long getRemainingCapacity() {
        return remainingCapacity;
    }

    public void setRemainingCapacity(final Long remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

    public Long getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxVmCapacity(final Long maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}
