package org.apache.cloudstack.api.command.admin.iso;

import com.cloud.api.response.TemplateResponse;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.command.user.iso.ListIsosCmd;

@APICommand(name = "listIsos", description = "Lists all available ISO files.", responseObject = TemplateResponse.class, responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListIsosCmdByAdmin extends ListIsosCmd {
}
