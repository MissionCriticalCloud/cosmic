package org.apache.cloudstack.api.response;

import com.cloud.network.as.AutoScalePolicy;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = AutoScalePolicy.class)
public class AutoScalePolicyResponse extends BaseResponse implements ControlledEntityResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the autoscale policy ID")
    private String id;

    @SerializedName(ApiConstants.ACTION)
    @Param(description = "the action to be executed if all the conditions evaluate to true for the specified duration.")
    private String action;

    @SerializedName(ApiConstants.DURATION)
    @Param(description = "the duration for which the conditions have to be true before action is taken")
    private Integer duration;

    @SerializedName(ApiConstants.QUIETTIME)
    @Param(description = "the cool down period for which the policy should not be evaluated after the action has been taken")
    private Integer quietTime;

    @SerializedName("conditions")
    @Param(description = "the list of IDs of the conditions that are being evaluated on every interval")
    private List<ConditionResponse> conditions;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account owning the autoscale policy")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id autoscale policy")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the autoscale policy")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID of the autoscale policy")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the autoscale policy")
    private String domainName;

    @Override
    public String getObjectId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setDuration(final Integer duration) {
        this.duration = duration;
    }

    public void setQuietTime(final Integer quietTime) {
        this.quietTime = quietTime;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public void setConditions(final List<ConditionResponse> conditions) {
        this.conditions = conditions;
    }

    @Override
    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    @Override
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    @Override
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }
}
