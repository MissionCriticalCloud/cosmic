package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class ResourceTagResponse extends BaseResponse implements ControlledViewEntityResponse {
    @SerializedName(ApiConstants.KEY)
    @Param(description = "tag key name")
    private String key;

    @SerializedName(ApiConstants.VALUE)
    @Param(description = "tag value")
    private String value;

    @SerializedName(ApiConstants.RESOURCE_TYPE)
    @Param(description = "resource type")
    private String resourceType;

    @SerializedName(ApiConstants.RESOURCE_ID)
    @Param(description = "id of the resource")
    private String resourceId;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account associated with the tag")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id the tag belongs to")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name where tag belongs to")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the ID of the domain associated with the tag")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain associated with the tag")
    private String domainName;

    @SerializedName(ApiConstants.CUSTOMER)
    @Param(description = "customer associated with the tag")
    private String customer;

    public void setValue(final String value) {
        this.value = value;
    }

    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    public void setResourceId(final String id) {
        this.resourceId = id;
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

    public void setCustomer(final String customer) {
        this.customer = customer;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final String key = this.getKey();
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResourceTagResponse other = (ResourceTagResponse) obj;
        final String key = this.getKey();
        if (key == null) {
            if (other.getKey() != null) {
                return false;
            }
        } else if (!key.equals(other.getKey())) {
            return false;
        }
        return true;
    }
}
