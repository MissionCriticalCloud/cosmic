package org.apache.cloudstack.api.command.admin.template;

import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.template.CreateTemplateCmd;
import org.apache.cloudstack.api.response.TemplateResponse;
import org.apache.cloudstack.context.CallContext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createTemplate", responseObject = TemplateResponse.class, description = "Creates a template of a virtual machine. " + "The virtual machine must be in a " +
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
