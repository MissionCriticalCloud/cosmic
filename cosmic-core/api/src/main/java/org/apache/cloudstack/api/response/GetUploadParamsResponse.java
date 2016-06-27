package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import java.net.URL;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

public class GetUploadParamsResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the template/volume ID")
    private UUID id;

    @SerializedName(ApiConstants.POST_URL)
    @Param(description = "POST url to upload the file to")
    private URL postURL;

    @SerializedName(ApiConstants.METADATA)
    @Param(description = "encrypted data to be sent in the POST request.")
    private String metadata;

    @SerializedName(ApiConstants.EXPIRES)
    @Param(description = "the timestamp after which the signature expires")
    private String expires;

    @SerializedName(ApiConstants.SIGNATURE)
    @Param(description = "signature to be sent in the POST request.")
    private String signature;

    public GetUploadParamsResponse(final UUID id, final URL postURL, final String metadata, final String expires, final String signature) {
        this.id = id;
        this.postURL = postURL;
        this.metadata = metadata;
        this.expires = expires;
        this.signature = signature;
        setObjectName("getuploadparams");
    }

    public GetUploadParamsResponse() {
        setObjectName("getuploadparams");
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setPostURL(final URL postURL) {
        this.postURL = postURL;
    }

    public void setMetadata(final String metadata) {
        this.metadata = metadata;
    }

    public void setTimeout(final String expires) {
        this.expires = expires;
    }

    public void setSignature(final String signature) {
        this.signature = signature;
    }
}
