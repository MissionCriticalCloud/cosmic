package com.cloud.api.command.user.project;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateProjectInvitation", group = APICommandGroup.ProjectService, description = "Accepts or declines project invitation", responseObject = SuccessResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateProjectInvitationCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateProjectInvitationCmd.class.getName());
    private static final String s_name = "updateprojectinvitationresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////
    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, required = true, description = "id of the project to join")
    private Long projectId;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "account that is joining the project")
    private String accountName;

    @Parameter(name = ApiConstants.TOKEN,
            type = CommandType.STRING,
            description = "list invitations for specified account; this parameter has to be specified with domainId")
    private String token;

    @Parameter(name = ApiConstants.ACCEPT, type = CommandType.BOOLEAN, description = "if true, accept the invitation, decline if false. True by default")
    private Boolean accept;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////
    public Long getProjectId() {
        return projectId;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getToken() {
        return token;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Project id: " + projectId + "; accountName " + accountName + "; accept " + getAccept());
        final boolean result = _projectService.updateInvitation(projectId, accountName, token, getAccept());
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to join the project");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////
    @Override
    public long getEntityOwnerId() {
        // TODO - return project entity ownerId
        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are
        // tracked
    }

    public Boolean getAccept() {
        if (accept == null) {
            return true;
        }
        return accept;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PROJECT_INVITATION_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Updating project invitation for projectId " + projectId;
    }
}
