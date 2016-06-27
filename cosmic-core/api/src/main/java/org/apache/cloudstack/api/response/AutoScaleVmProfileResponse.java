package org.apache.cloudstack.api.response;

import com.cloud.network.as.AutoScaleVmProfile;
import com.cloud.serializer.Param;
import com.cloud.utils.Pair;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd.CommandType;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;
import org.apache.cloudstack.api.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = AutoScaleVmProfile.class)
public class AutoScaleVmProfileResponse extends BaseResponse implements ControlledEntityResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the autoscale vm profile ID")
    private String id;

    /* Parameters related to deploy virtual machine */
    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the availability zone to be used while deploying a virtual machine")
    private String zoneId;

    @SerializedName(ApiConstants.SERVICE_OFFERING_ID)
    @Param(description = "the service offering to be used while deploying a virtual machine")
    private String serviceOfferingId;

    @SerializedName(ApiConstants.TEMPLATE_ID)
    @Param(description = "the template to be used while deploying a virtual machine")
    private String templateId;

    @SerializedName(ApiConstants.OTHER_DEPLOY_PARAMS)
    @Param(description = "parameters other than zoneId/serviceOfferringId/templateId to be used while deploying a virtual machine")
    private String otherDeployParams;

    /* Parameters related to destroying a virtual machine */
    @SerializedName(ApiConstants.AUTOSCALE_VM_DESTROY_TIME)
    @Param(description = "the time allowed for existing connections to get closed before a vm is destroyed")
    private Integer destroyVmGraceperiod;

    /* Parameters related to a running virtual machine - monitoring aspects */
    @SerializedName(ApiConstants.COUNTERPARAM_LIST)
    @Parameter(name = ApiConstants.COUNTERPARAM_LIST,
            type = CommandType.MAP,
            description = "counterparam list. Example: counterparam[0].name=snmpcommunity&counterparam[0].value=public&counterparam[1].name=snmpport&counterparam[1].value=161")
    private Map<String, String> counterParams;

    @SerializedName(ApiConstants.AUTOSCALE_USER_ID)
    @Param(description = "the ID of the user used to launch and destroy the VMs")
    private String autoscaleUserId;

    @Parameter(name = ApiConstants.CS_URL,
            type = CommandType.STRING,
            description = "the API URL including port of the CloudStack Management Server example: http://server.cloud.com:8080/client/api?")
    private String csUrl;

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
    @Param(description = "is profile for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    public AutoScaleVmProfileResponse() {
    }

    @Override
    public String getObjectId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public void setServiceOfferingId(final String serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    public void setTemplateId(final String templateId) {
        this.templateId = templateId;
    }

    public void setOtherDeployParams(final String otherDeployParams) {
        this.otherDeployParams = otherDeployParams;
    }

    public void setCounterParams(final List<Pair<String, String>> counterParams) {
        this.counterParams = new HashMap<>();
        for (final Pair<String, String> paramKV : counterParams) {
            final String key = paramKV.first();
            final String value = paramKV.second();
            this.counterParams.put(key, value);
        }
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

    public void setAutoscaleUserId(final String autoscaleUserId) {
        this.autoscaleUserId = autoscaleUserId;
    }

    public void setDestroyVmGraceperiod(final Integer destroyVmGraceperiod) {
        this.destroyVmGraceperiod = destroyVmGraceperiod;
    }

    public void setCsUrl(final String csUrl) {
        this.csUrl = csUrl;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
