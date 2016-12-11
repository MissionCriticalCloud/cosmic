package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

public class DeploymentPlannersResponse extends BaseResponse {
    @SerializedName(ApiConstants.NAME)
    @Param(description = "Deployment Planner name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
