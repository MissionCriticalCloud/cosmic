package org.apache.cloudstack.api;

import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.TemplatePermissionsResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseListTemplateOrIsoPermissionsCmd extends BaseCmd {
    protected static final String s_name = "listtemplatepermissionsresponse";
    public Logger logger = getLogger();

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = TemplatePermissionsResponse.class, required = true, description = "the template ID")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    protected Logger getLogger() {
        return LoggerFactory.getLogger(BaseListTemplateOrIsoPermissionsCmd.class);
    }

    protected boolean templateIsCorrectType(final VirtualMachineTemplate template) {
        return true;
    }

    public String getMediaType() {
        return "templateOrIso";
    }

    protected void executeWithView(final ResponseView view) {
        final List<String> accountNames = _templateService.listTemplatePermissions(this);

        final TemplatePermissionsResponse response = _responseGenerator.createTemplatePermissionsResponse(view, accountNames, id);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public long getEntityOwnerId() {
        final VirtualMachineTemplate template = _entityMgr.findById(VirtualMachineTemplate.class, getId());
        if (template != null) {
            return template.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    public Long getId() {
        return id;
    }
}
