package org.apache.cloudstack.api.command.admin.template;

import com.cloud.exception.ResourceAllocationException;
import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.template.RegisterTemplateCmd;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.TemplateResponse;

import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "registerTemplate", description = "Registers an existing template into the CloudStack cloud.", responseObject = TemplateResponse.class, responseView =
        ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class RegisterTemplateCmdByAdmin extends RegisterTemplateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(RegisterTemplateCmdByAdmin.class.getName());

    @Override
    public void execute() throws ResourceAllocationException {
        try {
            final VirtualMachineTemplate template = _templateService.registerTemplate(this);
            if (template != null) {
                final ListResponse<TemplateResponse> response = new ListResponse<>();
                final List<TemplateResponse> templateResponses = _responseGenerator.createTemplateResponses(ResponseView.Full, template, zoneId, false);
                response.setResponses(templateResponses);
                response.setResponseName(getCommandName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to register template");
            }
        } catch (final URISyntaxException ex1) {
            s_logger.info(ex1.toString());
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, ex1.getMessage());
        }
    }
}
