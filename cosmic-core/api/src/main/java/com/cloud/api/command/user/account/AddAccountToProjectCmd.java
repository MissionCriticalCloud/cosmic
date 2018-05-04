package com.cloud.api.command.user.account;

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
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.projects.Project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addAccountToProject", group = APICommandGroup.AccountService, description = "Adds account to a project", responseObject = SuccessResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddAccountToProjectCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddAccountToProjectCmd.class.getName());

    private static final String s_name = "addaccounttoprojectresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.PROJECT_ID,
            type = CommandType.UUID,
            entityType = ProjectResponse.class,
            required = true,
            description = "ID of the project to add the account to")
    private Long projectId;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "name of the account to be added to the project")
    private String accountName;

    @Parameter(name = ApiConstants.EMAIL, type = CommandType.STRING, description = "email to which invitation to the project is going to be sent")
    private String email;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        if (accountName == null && email == null) {
            throw new InvalidParameterValueException("Either accountName or email is required");
        }

        CallContext.current().setEventDetails("Project ID: " + projectId + "; accountName " + accountName);
        final boolean result = _projectService.addAccountToProject(getProjectId(), getAccountName(), getEmail());
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add account to the project");
        }
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getEmail() {
        return email;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Project project = _projectService.getProject(getProjectId());
        //verify input parameters
        if (project == null) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("Unable to find project with specified ID");
            ex.addProxyObject(getProjectId().toString(), "projectId");
            throw ex;
        }

        return _projectService.getProjectOwner(getProjectId()).getId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PROJECT_ACCOUNT_ADD;
    }

    @Override
    public String getEventDescription() {
        if (accountName != null) {
            return "Adding account " + getAccountName() + " to project: " + getProjectId();
        } else {
            return "Sending invitation to email " + email + " to join project: " + getProjectId();
        }
    }
}
