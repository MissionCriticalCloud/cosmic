package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class CreateCmdResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID)
    private String id;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}
