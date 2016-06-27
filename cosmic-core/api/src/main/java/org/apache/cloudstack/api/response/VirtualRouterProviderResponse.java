package org.apache.cloudstack.api.response;

import com.cloud.network.VirtualRouterProvider;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = VirtualRouterProvider.class)
public class VirtualRouterProviderResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the id of the router")
    private String id;

    @SerializedName(ApiConstants.NSP_ID)
    @Param(description = "the physical network service provider id of the provider")
    private String nspId;

    @SerializedName(ApiConstants.ENABLED)
    @Param(description = "Enabled/Disabled the service provider")
    private Boolean enabled;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account associated with the provider")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the ipaddress")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the address")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID associated with the provider")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain associated with the provider")
    private String domainName;

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
