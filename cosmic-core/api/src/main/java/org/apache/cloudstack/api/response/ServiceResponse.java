package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ServiceResponse extends BaseResponse {

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the service name")
    private String name;

    @SerializedName(ApiConstants.PROVIDER)
    @Param(description = "the service provider name", responseObject = ProviderResponse.class)
    private List<ProviderResponse> providers;

    @SerializedName("capability")
    @Param(description = "the list of capabilities", responseObject = CapabilityResponse.class)
    private List<CapabilityResponse> capabilities;

    public void setName(final String name) {
        this.name = name;
    }

    public void setCapabilities(final List<CapabilityResponse> capabilities) {
        this.capabilities = capabilities;
    }

    public void setProviders(final List<ProviderResponse> providers) {
        this.providers = providers;
    }
}
