package org.apache.cloudstack.api;

import com.cloud.exception.InvalidParameterValueException;
import org.apache.cloudstack.api.response.ProjectResponse;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.api.response.TemplateResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseUpdateTemplateOrIsoPermissionsCmd extends BaseCmd {
    public Logger _logger = getLogger();
    protected String _name = getResponseName();

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ACCOUNTS,
            type = CommandType.LIST,
            collectionType = CommandType.STRING,
            description = "a comma delimited list of accounts. If specified, \"op\" parameter has to be passed in.")
    private List<String> accountNames;
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = TemplateResponse.class, required = true, description = "the template ID")
    private Long id;
    @Parameter(name = ApiConstants.IS_FEATURED, type = CommandType.BOOLEAN, description = "true for featured template/iso, false otherwise")
    private Boolean featured;
    @Parameter(name = ApiConstants.IS_PUBLIC, type = CommandType.BOOLEAN, description = "true for public template/iso, false for private templates/isos")
    private Boolean isPublic;
    @Parameter(name = ApiConstants.IS_EXTRACTABLE,
            type = CommandType.BOOLEAN,
            description = "true if the template/iso is extractable, false other wise. Can be set only by root admin")
    private Boolean isExtractable;
    @Parameter(name = ApiConstants.OP, type = CommandType.STRING, description = "permission operator (add, remove, reset)")
    private String operation;
    @Parameter(name = ApiConstants.PROJECT_IDS,
            type = CommandType.LIST,
            collectionType = CommandType.UUID,
            entityType = ProjectResponse.class,
            description = "a comma delimited list of projects. If specified, \"op\" parameter has to be passed in.")
    private List<Long> projectIds;

    protected Logger getLogger() {
        return LoggerFactory.getLogger(BaseUpdateTemplateOrIsoPermissionsCmd.class);
    }

    protected String getResponseName() {
        return "updatetemplateorisopermissionsresponse";
    }

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public List<String> getAccountNames() {
        if (accountNames != null && projectIds != null) {
            throw new InvalidParameterValueException("Accounts and projectIds can't be specified together");
        }

        return accountNames;
    }

    public Long getId() {
        return id;
    }

    public Boolean isFeatured() {
        return featured;
    }

    public Boolean isPublic() {
        return isPublic;
    }

    public Boolean isExtractable() {
        return isExtractable;
    }

    public String getOperation() {
        return operation;
    }

    public List<Long> getProjectIds() {
        if (accountNames != null && projectIds != null) {
            throw new InvalidParameterValueException("Accounts and projectIds can't be specified together");
        }
        return projectIds;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        final boolean result = _templateService.updateTemplateOrIsoPermissions(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update template/iso permissions");
        }
    }

    @Override
    public String getCommandName() {
        return _name;
    }
}
