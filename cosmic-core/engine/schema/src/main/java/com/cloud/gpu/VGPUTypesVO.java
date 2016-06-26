package com.cloud.gpu;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "vgpu_types")
public class VGPUTypesVO implements InternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "gpu_group_id")
    private long gpuGroupId;

    @Column(name = "vgpu_type")
    private String vgpuType;

    @Column(name = "video_ram")
    private long videoRam;

    @Column(name = "max_heads")
    private long maxHeads;

    @Column(name = "max_resolution_x")
    private long maxResolutionX;

    @Column(name = "max_resolution_y")
    private long maxResolutionY;

    @Column(name = "max_vgpu_per_pgpu")
    private long maxVgpuPerPgpu;

    @Column(name = "remaining_capacity")
    private long remainingCapacity;

    @Column(name = "max_capacity")
    private long maxCapacity;

    protected VGPUTypesVO() {
    }

    public VGPUTypesVO(final long gpuGroupId, final String vgpuType, final long videoRam, final long maxHeads, final long maxResolutionX, final long maxResolutionY, final long
            maxVgpuPerPgpu,
                       final long remainingCapacity, final long maxCapacity) {
        this.gpuGroupId = gpuGroupId;
        this.vgpuType = vgpuType;
        this.videoRam = videoRam;
        this.maxHeads = maxHeads;
        this.maxResolutionX = maxResolutionX;
        this.maxResolutionY = maxResolutionY;
        this.maxVgpuPerPgpu = maxVgpuPerPgpu;
        this.remainingCapacity = remainingCapacity;
        this.maxCapacity = maxCapacity;
    }

    public long getGpuGroupId() {
        return gpuGroupId;
    }

    public void setGpuGroupId(final long gpuGroupId) {
        this.gpuGroupId = gpuGroupId;
    }

    public String getVgpuType() {
        return vgpuType;
    }

    public void setVgpuType(final String vgpuType) {
        this.vgpuType = vgpuType;
    }

    public long getVideoRam() {
        return videoRam;
    }

    public void setVideoRam(final long videoRam) {
        this.videoRam = videoRam;
    }

    public long getMaxHeads() {
        return maxHeads;
    }

    public void setMaxHeads(final long maxHeads) {
        this.maxHeads = maxHeads;
    }

    public long getMaxResolutionX() {
        return maxResolutionX;
    }

    public void setMaxResolutionX(final long maxResolutionX) {
        this.maxResolutionX = maxResolutionX;
    }

    public long getMaxResolutionY() {
        return maxResolutionY;
    }

    public void setMaxResolutionY(final long maxResolutionY) {
        this.maxResolutionY = maxResolutionY;
    }

    public long getMaxVgpuPerPgpu() {
        return maxVgpuPerPgpu;
    }

    public void setMaxVgpuPerPgpu(final long maxVgpuPerPgpu) {
        this.maxVgpuPerPgpu = maxVgpuPerPgpu;
    }

    public long getRemainingCapacity() {
        return remainingCapacity;
    }

    public void setRemainingCapacity(final long remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

    public long getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(final long maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    @Override
    public long getId() {
        return id;
    }
}
