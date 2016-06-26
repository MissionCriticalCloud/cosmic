package com.cloud.server.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class BaremetalTemplateResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the template ID")
    private String id;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}
