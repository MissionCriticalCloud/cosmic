package org.apache.cloudstack.api.response;

import com.cloud.network.as.AutoScaleVmGroup;
import com.cloud.serializer.Param;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = AutoScaleVmGroup.class)
public class AutoScaleVmGroupResponse extends BaseResponse implements ControlledEntityResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the autoscale vm group ID")
    private String id;

    @SerializedName(ApiConstants.LBID)
    @Param(description = "the load balancer rule ID")
    private String loadBalancerId;

    @SerializedName(ApiConstants.VMPROFILE_ID)
    @Param(description = "the autoscale profile that contains information about the vms in the vm group.")
    private String profileId;

    @SerializedName(ApiConstants.MIN_MEMBERS)
    @Param(description = "the minimum number of members in the vmgroup, the number of instances in the vm group will be equal to or more than this number.")
    private int minMembers;

    @SerializedName(ApiConstants.MAX_MEMBERS)
    @Param(description = "the maximum number of members in the vmgroup, The number of instances in the vm group will be equal to or less than this number.")
    private int maxMembers;

    @SerializedName(ApiConstants.INTERVAL)
    @Param(description = "the frequency at which the conditions have to be evaluated")
    private int interval;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the current state of the AutoScale Vm Group")
    private String state;

    @SerializedName(ApiConstants.SCALEUP_POLICIES)
    @Param(description = "list of scaleup autoscale policies")
    private List<AutoScalePolicyResponse> scaleUpPolicies;

    @SerializedName(ApiConstants.SCALEDOWN_POLICIES)
    @Param(description = "list of scaledown autoscale policies")
    private List<AutoScalePolicyResponse> scaleDownPolicies;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account owning the instance group")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id vm profile")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the vm profile")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID of the vm profile")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the vm profile")
    private String domainName;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is group for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    public AutoScaleVmGroupResponse() {

    }

    @Override
    public String getObjectId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setLoadBalancerId(final String loadBalancerId) {
        this.loadBalancerId = loadBalancerId;
    }

    public void setProfileId(final String profileId) {
        this.profileId = profileId;
    }

    public void setMinMembers(final int minMembers) {
        this.minMembers = minMembers;
    }

    public void setMaxMembers(final int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setInterval(final int interval) {
        this.interval = interval;
    }

    public void setScaleUpPolicies(final List<AutoScalePolicyResponse> scaleUpPolicies) {
        this.scaleUpPolicies = scaleUpPolicies;
    }

    public void setScaleDownPolicies(final List<AutoScalePolicyResponse> scaleDownPolicies) {
        this.scaleDownPolicies = scaleDownPolicies;
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

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
