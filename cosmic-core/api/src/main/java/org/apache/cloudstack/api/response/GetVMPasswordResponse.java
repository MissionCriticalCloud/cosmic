package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class GetVMPasswordResponse extends BaseResponse {

    @SerializedName("encryptedpassword")
    @Param(description = "The base64 encoded encrypted password of the VM", isSensitive = true)
    private String encryptedPassword;

    public GetVMPasswordResponse() {
    }

    public GetVMPasswordResponse(final String responseName, final String encryptedPassword) {
        setResponseName(responseName);
        setObjectName("password");
        setEncryptedPassword(encryptedPassword);
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(final String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }
}
