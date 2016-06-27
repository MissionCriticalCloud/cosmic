package org.apache.cloudstack.api.response;

import com.cloud.network.VirtualRouterProvider;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = VirtualRouterProvider.class)
public class InternalLoadBalancerElementResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the id of the internal load balancer element")
    private String id;

    @SerializedName(ApiConstants.NSP_ID)
    @Param(description = "the physical network service provider id of the element")
    private String nspId;

    @SerializedName(ApiConstants.ENABLED)
    @Param(description = "Enabled/Disabled the element")
    private Boolean enabled;

    public void setId(final String id) {
        this.id = id;
    }

    public void setNspId(final String nspId) {
        this.nspId = nspId;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }
}
