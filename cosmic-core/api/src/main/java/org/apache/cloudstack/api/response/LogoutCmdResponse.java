package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;

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
