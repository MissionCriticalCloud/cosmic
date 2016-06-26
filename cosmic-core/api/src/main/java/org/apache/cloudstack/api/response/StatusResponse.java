package org.apache.cloudstack.api.response;

import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class StatusResponse extends BaseResponse {
    @SerializedName("status")
    private Boolean status;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(final Boolean status) {
        this.status = status;
    }
}
