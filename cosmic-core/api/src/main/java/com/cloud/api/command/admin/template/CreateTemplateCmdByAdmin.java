package com.cloud.api.command.admin.template;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.template.CreateTemplateCmd;
import com.cloud.api.response.TemplateResponse;
import com.cloud.context.CallContext;
import com.cloud.template.VirtualMachineTemplate;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createTemplate", group = APICommandGroup.TemplateService, responseObject = TemplateResponse.class, description = "Creates a template of a virtual machine. " + "The virtual machine must be in a " +
        "STOPPED state. "
        + "A template created from this command is automatically designated as a private template visible to the account that created it.", responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateTemplateCmdByAdmin extends CreateTemplateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateTemplateCmdByAdmin.class.getName());

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Template Id: " + getEntityId() + ((getSnapshotId() == null) ? " from volume Id: " + getVolumeId() : " from snapshot Id: " +
                getSnapshotId()));
        VirtualMachineTemplate template = null;
        template = _templateService.createPrivateTemplate(this);

        if (template != null) {
            final List<TemplateResponse> templateResponses = _responseGenerator.createTemplateResponses(ResponseView.Full, template.getId(), snapshotId, volumeId, false);
            TemplateResponse response = new TemplateResponse();
            if (templateResponses != null && !templateResponses.isEmpty()) {
                response = templateResponses.get(0);
            }
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create private template");
        }
    }
}
