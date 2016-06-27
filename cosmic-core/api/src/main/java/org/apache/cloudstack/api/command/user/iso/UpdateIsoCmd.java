package org.apache.cloudstack.api.command.user.iso;

import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseUpdateTemplateOrIsoCmd;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.TemplateResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateIso", description = "Updates an ISO file.", responseObject = TemplateResponse.class, responseView = ResponseView.Restricted,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateIsoCmd extends BaseUpdateTemplateOrIsoCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateIsoCmd.class.getName());
    private static final String s_name = "updateisoresponse";

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public Boolean getRequiresHvm() {
        return null;
    }

    @Override
    public Boolean getPasswordEnabled() {
        return null;
    }

    @Override
    public String getFormat() {
        return null;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final VirtualMachineTemplate result = _templateService.updateTemplate(this);
        if (result != null) {
            final TemplateResponse response = _responseGenerator.createTemplateUpdateResponse(ResponseView.Restricted, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update ISO");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final VirtualMachineTemplate template = _entityMgr.findById(VirtualMachineTemplate.class, getId());
        if (template != null) {
            return template.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
