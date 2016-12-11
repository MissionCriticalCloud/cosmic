package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

public class LogoutCmdResponse extends AuthenticationCmdResponse {
    @SerializedName(value = ApiConstants.DESCRIPTION)
    @Param(description = "Response description")
    private String description;

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
