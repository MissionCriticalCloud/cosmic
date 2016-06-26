package org.apache.cloudstack.api.response;

import com.cloud.projects.ProjectAccount;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = ProjectAccount.class)
public class ProjectAccountResponse extends BaseResponse implements ControlledViewEntityResponse {
    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "project id")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "project name")
    private String projectName;

    @SerializedName(ApiConstants.ACCOUNT_ID)
    @Param(description = "the id of the account")
    private String accountId;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the name of the account")
    private String accountName;

    @SerializedName(ApiConstants.ACCOUNT_TYPE)
    @Param(description = "account type (admin, domain-admin, user)")
    private Short accountType;

    @SerializedName(ApiConstants.ROLE)
    @Param(description = "account role in the project (regular,owner)")
    private String role;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "id of the Domain the account belongs too")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "name of the Domain the account belongs too")
    private String domainName;

    @SerializedName(ApiConstants.USER)
    @Param(description = "the list of users associated with account", responseObject = UserResponse.class)
    private List<UserResponse> users;

    public void setAccountId(final String id) {
        this.accountId = id;
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

    public void setAccountType(final Short accountType) {
        this.accountType = accountType;
    }

    public void setUsers(final List<UserResponse> users) {
        this.users = users;
    }

    public void setRole(final String role) {
        this.role = role;
    }
}
