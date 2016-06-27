package org.apache.cloudstack.api.command.user.project;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.projects.Project;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.ProjectResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createProject", description = "Creates a project", responseObject = ProjectResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateProjectCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateProjectCmd.class.getName());

    private static final String s_name = "createprojectresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "account who will be Admin for the project")
    private String accountName;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "domain ID of the account owning a project")
    private Long domainId;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "name of the project")
    private String name;

    @Parameter(name = ApiConstants.DISPLAY_TEXT, type = CommandType.STRING, required = true, description = "display text of the project")
    private String displayText;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        final Project project = _projectService.enableProject(this.getEntityId());
        if (project != null) {
            final ProjectResponse response = _responseGenerator.createProjectResponse(project);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create a project");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Account caller = CallContext.current().getCallingAccount();

        if ((accountName != null && domainId == null) || (domainId != null && accountName == null)) {
            throw new InvalidParameterValueException("Account name and domain id must be specified together");
        }

        if (accountName != null) {
            return _accountService.finalizeOwner(caller, accountName, domainId, null).getId();
        }

        return caller.getId();
    }

    @Override
    public void create() throws ResourceAllocationException {
        CallContext.current().setEventDetails("Project Name: " + getName());
        final Project project = _projectService.createProject(getName(), getDisplayText(), getAccountName(), getDomainId());
        if (project != null) {
            this.setEntityId(project.getId());
            this.setEntityUuid(project.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create a project");
        }
    }

    public String getName() {
        return name;
    }

    public String getDisplayText() {
        return displayText;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    public String getAccountName() {
        if (accountName != null) {
            return accountName;
        } else {
            return CallContext.current().getCallingAccount().getAccountName();
        }
    }

    public Long getDomainId() {
        if (domainId != null) {
            return domainId;
        } else {
            return CallContext.current().getCallingAccount().getDomainId();
        }
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PROJECT_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "creating project";
    }
}
