package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.serializer.Param;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class GpuResponse extends BaseResponse {

    @SerializedName(ApiConstants.GPUGROUPNAME)
    @Param(description = "GPU cards present in the host")
    private String gpuGroupName;

    @SerializedName(ApiConstants.VGPU)
    @Param(description = "the list of enabled vGPUs", responseObject = VgpuResponse.class)
    private List<VgpuResponse> vgpu;

    public void setGpuGroupName(final String gpuGroupName) {
        this.gpuGroupName = gpuGroupName;
    }

    public void setVgpu(final List<VgpuResponse> vgpu) {
        this.vgpu = vgpu;
    }
}
