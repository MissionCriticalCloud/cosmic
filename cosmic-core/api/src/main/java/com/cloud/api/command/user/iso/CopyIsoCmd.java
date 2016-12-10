package com.cloud.api.command.user.iso;

import com.cloud.api.command.user.template.CopyTemplateCmd;
import com.cloud.api.response.TemplateResponse;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ResponseObject.ResponseView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "copyIso", description = "Copies an ISO from one zone to another.", responseObject = TemplateResponse.class, responseView = ResponseView.Restricted,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CopyIsoCmd extends CopyTemplateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CopyIsoCmd.class.getName());
    private static final String s_name = "copyisoresponse";
}
