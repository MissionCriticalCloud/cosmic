package com.cloud.api.command.user.account;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.project.DeleteProjectCmd;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.projects.Project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteAccountFromProject", group = APICommandGroup.AccountService, description = "Deletes account from the project", responseObject = SuccessResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteAccountFromProjectCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteProjectCmd.class.getName());

    private static final String s_name = "deleteaccountfromprojectresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.PROJECT_ID,
            type = CommandType.UUID,
            entityType = ProjectResponse.class,
            required = true,
            description = "ID of the project to remove the account from")
    private Long projectId;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, required = true, description = "name of the account to be removed from the project")
    private String accountName;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getProjectId() {
        return projectId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Project ID: " + projectId + "; accountName " + accountName);
        final boolean result = _projectService.deleteAccountFromProject(projectId, accountName);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete account from the project");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Project project = _projectService.getProject(projectId);
        //verify input parameters
        if (project == null) {
            throw new InvalidParameterValueException("Unable to find project by ID " + projectId);
        }

        return _projectService.getProjectOwner(projectId).getId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PROJECT_ACCOUNT_REMOVE;
    }

    @Override
    public String getEventDescription() {
        return "Removing account " + accountName + " from project: " + projectId;
    }
}
