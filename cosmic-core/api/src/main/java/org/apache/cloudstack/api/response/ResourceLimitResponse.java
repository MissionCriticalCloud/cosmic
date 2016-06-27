package org.apache.cloudstack.api.response;

import com.cloud.configuration.ResourceLimit;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = ResourceLimit.class)
public class ResourceLimitResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account of the resource limit")
    private String accountName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID of the resource limit")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the resource limit")
    private String domainName;

    @SerializedName(ApiConstants.RESOURCE_TYPE)
    @Param(description = "resource type. Values include 0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11. See the resourceType parameter for more information on these values.")
    private String resourceType;

    @SerializedName("max")
    @Param(description = "the maximum number of the resource. A -1 means the resource currently has no limit.")
    private Long max;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the resource limit")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the resource limit")
    private String projectName;

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

    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    public void setMax(final Long max) {
        this.max = max;
    }
}
