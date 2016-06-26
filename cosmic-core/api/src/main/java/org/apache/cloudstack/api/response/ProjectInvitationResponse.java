package org.apache.cloudstack.api.response;

import com.cloud.projects.ProjectInvitation;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = ProjectInvitation.class)
public class ProjectInvitationResponse extends BaseResponse implements ControlledViewEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the id of the invitation")
    private String id;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the id of the project")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the name of the project")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain id the project belongs to")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name where the project belongs to")
    private String domainName;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account name of the project's owner")
    private String accountName;

    @SerializedName(ApiConstants.EMAIL)
    @Param(description = "the email the invitation was sent to")
    private String email;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the invitation state")
    private String invitationState;

    public void setId(final String id) {
        this.id = id;
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
    public void setDomainName(final String domain) {
        this.domainName = domain;
    }

    public void setInvitationState(final String invitationState) {
        this.invitationState = invitationState;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
