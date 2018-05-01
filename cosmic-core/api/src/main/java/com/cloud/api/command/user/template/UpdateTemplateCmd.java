package com.cloud.api.command.user.template;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseUpdateTemplateOrIsoCmd;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.TemplateResponse;
import com.cloud.legacymodel.user.Account;
import com.cloud.template.VirtualMachineTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateTemplate", group = APICommandGroup.TemplateService, description = "Updates attributes of a template.", responseObject = TemplateResponse.class, responseView = ResponseView
        .Restricted,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateTemplateCmd extends BaseUpdateTemplateOrIsoCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateTemplateCmd.class.getName());
    private static final String s_name = "updatetemplateresponse";

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public Boolean getBootable() {
        return null;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public TemplateResponse getResponse() {
        return null;
    }

    @Override
    public void execute() {
        final VirtualMachineTemplate result = _templateService.updateTemplate(this);
        if (result != null) {
            final TemplateResponse response = _responseGenerator.createTemplateUpdateResponse(ResponseView.Restricted, result);
            response.setObjectName("template");
            response.setTemplateType(result.getTemplateType().toString());//Template can be either USER or ROUTING type
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update template");
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
