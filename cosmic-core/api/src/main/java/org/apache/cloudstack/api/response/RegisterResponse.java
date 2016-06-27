package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse extends BaseResponse {
    @SerializedName("apikey")
    @Param(description = "the api key of the registered user", isSensitive = true)
    private String apiKey;

    @SerializedName("secretkey")
    @Param(description = "the secret key of the registered user", isSensitive = true)
    private String secretKey;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }
}
