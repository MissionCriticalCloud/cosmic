package org.apache.cloudstack.api.response;

import com.cloud.network.rules.HealthCheckPolicy;
import com.cloud.serializer.Param;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class LBHealthCheckPolicyResponse extends BaseResponse {
    @SerializedName("responsetime")
    @Param(description = "Time to wait when receiving a response from the health check")
    private final int responseTime;
    @SerializedName("healthcheckinterval")
    @Param(description = "Amount of time between health checks")
    private final int healthcheckInterval;
    @SerializedName("healthcheckthresshold")
    @Param(description = "Number of consecutive health check success before declaring an instance healthy")
    private final int healthcheckthresshold;
    @SerializedName("unhealthcheckthresshold")
    @Param(description = "Number of consecutive health check failures before declaring an instance unhealthy.")
    private final int unhealthcheckthresshold;
    @SerializedName("id")
    @Param(description = "the LB HealthCheck policy ID")
    private String id;
    @SerializedName("pingpath")
    @Param(description = "the pingpath  of the healthcheck policy")
    private String pingpath;
    @SerializedName("description")
    @Param(description = "the description of the healthcheck policy")
    private String description;
    @SerializedName("state")
    @Param(description = "the state of the policy")
    private String state;
    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is policy for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    public LBHealthCheckPolicyResponse(final HealthCheckPolicy healthcheckpolicy) {
        if (healthcheckpolicy.isRevoke()) {
            this.setState("Revoked");
        }
        if (healthcheckpolicy.getUuid() != null) {
            setId(healthcheckpolicy.getUuid());
        }
        this.pingpath = healthcheckpolicy.getpingpath();
        this.healthcheckInterval = healthcheckpolicy.getHealthcheckInterval();
        this.responseTime = healthcheckpolicy.getResponseTime();
        this.healthcheckthresshold = healthcheckpolicy.getHealthcheckThresshold();
        this.unhealthcheckthresshold = healthcheckpolicy.getUnhealthThresshold();
        this.forDisplay = healthcheckpolicy.isDisplay();
        this.description = healthcheckpolicy.getDescription();
        setObjectName("healthcheckpolicy");
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getpingpath() {
        return pingpath;
    }

    public void setpingpath(final String pingpath) {
        this.pingpath = pingpath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
