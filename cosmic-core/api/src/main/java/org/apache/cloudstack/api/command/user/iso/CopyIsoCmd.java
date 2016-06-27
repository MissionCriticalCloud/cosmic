package org.apache.cloudstack.api.command.user.iso;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.command.user.template.CopyTemplateCmd;
import org.apache.cloudstack.api.response.TemplateResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "copyIso", description = "Copies an ISO from one zone to another.", responseObject = TemplateResponse.class, responseView = ResponseView.Restricted,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CopyIsoCmd extends CopyTemplateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CopyIsoCmd.class.getName());
    private static final String s_name = "copyisoresponse";
}
