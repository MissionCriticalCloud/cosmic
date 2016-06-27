package org.apache.cloudstack.api.command.admin.iso;

import com.cloud.exception.ResourceAllocationException;
import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.iso.RegisterIsoCmd;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.TemplateResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "registerIso", responseObject = TemplateResponse.class, description = "Registers an existing ISO into the CloudStack Cloud.", responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class RegisterIsoCmdByAdmin extends RegisterIsoCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(RegisterIsoCmdByAdmin.class.getName());

    @Override
    public void execute() throws ResourceAllocationException {
        final VirtualMachineTemplate template = _templateService.registerIso(this);
        if (template != null) {
            final ListResponse<TemplateResponse> response = new ListResponse<>();
            final List<TemplateResponse> templateResponses = _responseGenerator.createIsoResponses(ResponseView.Full, template, zoneId, false);
            response.setResponses(templateResponses);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to register iso");
        }
    }
}
