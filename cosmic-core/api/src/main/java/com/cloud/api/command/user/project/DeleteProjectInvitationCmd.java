package com.cloud.api.command.user.project;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ProjectInvitationResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteProjectInvitation", description = "Deletes project invitation", responseObject = SuccessResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteProjectInvitationCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteProjectInvitationCmd.class.getName());
    private static final String s_name = "deleteprojectinvitationresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = ProjectInvitationResponse.class, required = true, description = "id of the invitation")
    private Long id;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////
    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Project invitation id " + id);
        final boolean result = _projectService.deleteProjectInvitation(id);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete the project invitation");
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

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PROJECT_INVITATION_REMOVE;
    }

    @Override
    public String getEventDescription() {
        return "Project invitatino id " + id + " is being removed";
    }
}
