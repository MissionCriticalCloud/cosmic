package com.cloud.api.command.admin.iso;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.command.user.iso.ListIsosCmd;
import com.cloud.api.response.TemplateResponse;

@APICommand(name = "listIsos", group = APICommandGroup.ISOService, description = "Lists all available ISO files.", responseObject = TemplateResponse.class, responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListIsosCmdByAdmin extends ListIsosCmd {
}
