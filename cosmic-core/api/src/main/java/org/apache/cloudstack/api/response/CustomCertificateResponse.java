package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class CustomCertificateResponse extends BaseResponse {

    @SerializedName("message")
    @Param(description = "message of the certificate upload operation")
    private String message;

    public String getResultMessage() {
        return message;
    }

    public void setResultMessage(final String msg) {
        this.message = msg;
    }
}
