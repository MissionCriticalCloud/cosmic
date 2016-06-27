package org.apache.cloudstack.api.response;

import com.cloud.network.VpnUser;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = VpnUser.class)
public class VpnUsersResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the vpn userID")
    private String id;

    @SerializedName(ApiConstants.USERNAME)
    @Param(description = "the username of the vpn user")
    private String userName;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account of the remote access vpn")
    private String accountName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain id of the account of the remote access vpn")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the account of the remote access vpn")
    private String domainName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the vpn")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the vpn")
    private String projectName;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the Vpn User")
    private String state;

    public void setId(final String id) {
        this.id = id;
    }

    public void setUserName(final String name) {
        this.userName = name;
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
    public void setDomainName(final String name) {
        this.domainName = name;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }
}
