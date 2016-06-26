package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class ResourceCountResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account for which resource count's are updated")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id for which resource count's are updated")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name for which resource count's are updated")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID for which resource count's are updated")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name for which resource count's are updated")
    private String domainName;

    @SerializedName(ApiConstants.RESOURCE_TYPE)
    @Param(description = "resource type. Values include 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11. See the resourceType parameter for more information on these values.")
    private String resourceType;

    @SerializedName("resourcecount")
    @Param(description = "resource count")
    private long resourceCount;

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

    public void setResourceCount(final Long resourceCount) {
        this.resourceCount = resourceCount;
    }
}
