package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

/**
 * Load Balancer instance is the User Vm instance participating in the Load Balancer
 */

public class ApplicationLoadBalancerInstanceResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the instance ID")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the instance")
    private String name;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the instance")
    private String state;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "the ip address of the instance")
    private String ipAddress;

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
