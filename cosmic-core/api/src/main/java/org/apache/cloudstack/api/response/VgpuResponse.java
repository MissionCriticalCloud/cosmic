package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class VgpuResponse extends BaseResponse {

    @SerializedName(ApiConstants.VGPUTYPE)
    @Param(description = "Model Name of vGPU")
    private String name;

    @SerializedName(ApiConstants.VIDEORAM)
    @Param(description = "Video RAM for this vGPU type")
    private Long videoRam;

    @SerializedName(ApiConstants.MAXHEADS)
    @Param(description = "Maximum displays per user")
    private Long maxHeads;

    @SerializedName(ApiConstants.MAXRESOLUTIONX)
    @Param(description = "Maximum X resolution per display")
    private Long maxResolutionX;

    @SerializedName(ApiConstants.MAXRESOLUTIONY)
    @Param(description = "Maximum Y resolution per display")
    private Long maxResolutionY;

    @SerializedName(ApiConstants.MAXVGPUPERPGPU)
    @Param(description = "Maximum no. of vgpu per gpu card (pgpu)")
    private Long maxVgpuPerPgpu;

    @SerializedName(ApiConstants.REMAININGCAPACITY)
    @Param(description = "Remaining capacity in terms of no. of more VMs that can be deployped with this vGPU type")
    private Long remainingCapacity;

    @SerializedName(ApiConstants.MAXCAPACITY)
    @Param(description = "Maximum vgpu can be created with this vgpu type on the given gpu group")
    private Long maxCapacity;

    public void setName(final String name) {
        this.name = name;
    }

    public void setVideoRam(final Long videoRam) {
        this.videoRam = videoRam;
    }

    public void setMaxHeads(final Long maxHeads) {
        this.maxHeads = maxHeads;
    }

    public void setMaxResolutionX(final Long maxResolutionX) {
        this.maxResolutionX = maxResolutionX;
    }

    public void setMaxResolutionY(final Long maxResolutionY) {
        this.maxResolutionY = maxResolutionY;
    }

    public void setMaxVgpuPerPgpu(final Long maxVgpuPerPgpu) {
        this.maxVgpuPerPgpu = maxVgpuPerPgpu;
    }

    public void setRemainingCapacity(final Long remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

    public void setmaxCapacity(final Long maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}
