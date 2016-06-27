package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class IsolationMethodResponse extends BaseResponse {
    @SerializedName(ApiConstants.NAME)
    @Param(description = "Network isolation method name")
    private String name;

    public void setIsolationMethodName(final String isolationMethodName) {
        this.name = isolationMethodName;
    }
}
