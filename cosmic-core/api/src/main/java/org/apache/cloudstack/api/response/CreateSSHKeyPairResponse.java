package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

public class CreateSSHKeyPairResponse extends SSHKeyPairResponse {

    @SerializedName("privatekey")
    @Param(description = "Private key", isSensitive = true)
    private String privateKey;

    public CreateSSHKeyPairResponse() {
    }

    public CreateSSHKeyPairResponse(final String name, final String fingerprint, final String privateKey) {
        super(name, fingerprint);
        this.privateKey = privateKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(final String privateKey) {
        this.privateKey = privateKey;
    }
}
