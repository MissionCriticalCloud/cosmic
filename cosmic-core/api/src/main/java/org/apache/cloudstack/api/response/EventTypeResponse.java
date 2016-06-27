package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class EventTypeResponse extends BaseResponse {
    @SerializedName(ApiConstants.NAME)
    @Param(description = "Event Type")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
