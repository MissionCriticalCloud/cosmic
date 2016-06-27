package org.apache.cloudstack.api.command.admin.template;

import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.template.UpdateTemplateCmd;
import org.apache.cloudstack.api.response.TemplateResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateTemplate", description = "Updates attributes of a template.", responseObject = TemplateResponse.class, responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateTemplateCmdByAdmin extends UpdateTemplateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateTemplateCmdByAdmin.class.getName());

    @Override
    public void execute() {
        final VirtualMachineTemplate result = _templateService.updateTemplate(this);
        if (result != null) {
            final TemplateResponse response = _responseGenerator.createTemplateUpdateResponse(ResponseView.Full, result);
            response.setObjectName("template");
            response.setTemplateType(result.getTemplateType().toString());//Template can be either USER or ROUTING type
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update template");
        }
    }
}
