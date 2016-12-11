package com.cloud.api.command.user.project;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.projects.Project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteProject", description = "Deletes a project", responseObject = SuccessResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteProjectCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteProjectCmd.class.getName());

    private static final String s_name = "deleteprojectresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = ProjectResponse.class, required = true, description = "id of the project to be deleted")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long geId() {
        return id;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Project Id: " + id);
        final boolean result = _projectService.deleteProject(id);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete project");
        }
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
        final Project project = _projectService.getProject(id);
        //verify input parameters
        if (project == null) {
            throw new InvalidParameterValueException("Unable to find project by id " + id);
        }

        return _projectService.getProjectOwner(id).getId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PROJECT_DELETE;
    }

    @Override
    public String getEventDescription() {
        return "Deleting project: " + id;
    }
}
