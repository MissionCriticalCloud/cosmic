package com.cloud.api.command.admin.iso;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.iso.UpdateIsoCmd;
import com.cloud.api.response.TemplateResponse;
import com.cloud.template.VirtualMachineTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateIso", group = APICommandGroup.ISOService, description = "Updates an ISO file.", responseObject = TemplateResponse.class, responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateIsoCmdByAdmin extends UpdateIsoCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateIsoCmdByAdmin.class.getName());

    @Override
    public void execute() {
        final VirtualMachineTemplate result = _templateService.updateTemplate(this);
        if (result != null) {
            final TemplateResponse response = _responseGenerator.createTemplateUpdateResponse(ResponseView.Full, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update iso");
        }
    }
}
