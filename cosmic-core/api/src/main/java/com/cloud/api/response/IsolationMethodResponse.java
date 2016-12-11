package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

public class IsolationMethodResponse extends BaseResponse {
    @SerializedName(ApiConstants.NAME)
    @Param(description = "Network isolation method name")
    private String name;

    public void setIsolationMethodName(final String isolationMethodName) {
        this.name = isolationMethodName;
    }
}
