package com.cloud.agent.api.to;

import com.cloud.agent.api.VgpuTypesInfo;

import java.util.HashMap;

public class GPUDeviceTO {

    private String gpuGroup;
    private String vgpuType;
    private HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails = new HashMap<>();

    public GPUDeviceTO(final String gpuGroup, final String vgpuType, final HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails) {
        this.gpuGroup = gpuGroup;
        this.vgpuType = vgpuType;
        this.groupDetails = groupDetails;
    }

    public String getGpuGroup() {
        return gpuGroup;
    }

    public void setGpuGroup(final String gpuGroup) {
        this.gpuGroup = gpuGroup;
    }

    public String getVgpuType() {
        return vgpuType;
    }

    public void setVgpuType(final String vgpuType) {
        this.vgpuType = vgpuType;
    }

    public HashMap<String, HashMap<String, VgpuTypesInfo>> getGroupDetails() {
        return groupDetails;
    }

    public void setGroupDetails(final HashMap<String, HashMap<String, VgpuTypesInfo>> groupDetails) {
        this.groupDetails = groupDetails;
    }
}
