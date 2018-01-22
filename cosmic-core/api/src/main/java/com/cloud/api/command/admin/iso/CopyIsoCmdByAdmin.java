package com.cloud.api.command.admin.iso;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.command.user.iso.CopyIsoCmd;
import com.cloud.api.response.TemplateResponse;

@APICommand(name = "copyIso", group = APICommandGroup.ISOService, description = "Copies an iso from one zone to another.", responseObject = TemplateResponse.class, responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CopyIsoCmdByAdmin extends CopyIsoCmd {

}
