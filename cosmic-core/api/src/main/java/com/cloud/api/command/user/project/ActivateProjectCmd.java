package com.cloud.api.command.user.project;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ProjectResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.projects.Project;
import com.cloud.utils.exception.InvalidParameterValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "activateProject", group = APICommandGroup.ProjectService, description = "Activates a project", responseObject = ProjectResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ActivateProjectCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ActivateProjectCmd.class.getName());

    private static final String s_name = "activaterojectresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = ProjectResponse.class, required = true, description = "id of the project to be modified")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Project id: " + getId());
        final Project project = _projectService.activateProject(getId());
        if (project != null) {
            final ProjectResponse response = _responseGenerator.createProjectResponse(project);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to activate a project");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Project project = _projectService.getProject(getId());
        //verify input parameters
        if (project == null) {
            throw new InvalidParameterValueException("Unable to find project by id " + getId());
        }

        return _projectService.getProjectOwner(getId()).getId();
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PROJECT_ACTIVATE;
    }

    @Override
    public String getEventDescription() {
        return "Activating project: " + id;
    }
}
