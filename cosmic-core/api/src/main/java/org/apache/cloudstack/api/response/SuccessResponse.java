package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class SuccessResponse extends BaseResponse {
    @SerializedName("success")
    @Param(description = "true if operation is executed successfully")
    private Boolean success = true;

    @SerializedName("displaytext")
    @Param(description = "any text associated with the success or failure")
    private String displayText;

    public SuccessResponse() {
    }

    public SuccessResponse(final String responseName) {
        super.setResponseName(responseName);
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(final Boolean success) {
        this.success = success;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }
}
