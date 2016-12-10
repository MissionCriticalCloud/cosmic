package org.apache.cloudstack.api.command.admin.template;

import com.cloud.api.response.TemplateResponse;
import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.command.user.template.ListTemplatesCmd;

@APICommand(name = "listTemplates", description = "List all public, private, and privileged templates.", responseObject = TemplateResponse.class, entityType =
        {VirtualMachineTemplate.class}, responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListTemplatesCmdByAdmin extends ListTemplatesCmd {

}
