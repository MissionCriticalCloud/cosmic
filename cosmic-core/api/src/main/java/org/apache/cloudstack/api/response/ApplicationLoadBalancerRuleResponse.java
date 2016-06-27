package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

/**
 * Subobject of the load balancer container response
 */
public class ApplicationLoadBalancerRuleResponse extends BaseResponse {
    @SerializedName(ApiConstants.SOURCE_PORT)
    @Param(description = "source port of the load balancer rule")
    private Integer sourcePort;

    @SerializedName(ApiConstants.INSTANCE_PORT)
    @Param(description = "instance port of the load balancer rule")
    private Integer instancePort;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the load balancer rule")
    private String state;

    public void setSourcePort(final Integer sourcePort) {
        this.sourcePort = sourcePort;
    }

    public void setInstancePort(final Integer instancePort) {
        this.instancePort = instancePort;
    }

    public void setState(final String state) {
        this.state = state;
    }
}
