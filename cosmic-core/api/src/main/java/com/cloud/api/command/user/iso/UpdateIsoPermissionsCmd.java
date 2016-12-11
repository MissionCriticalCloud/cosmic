package com.cloud.api.command.user.iso;

import com.cloud.api.APICommand;
import com.cloud.api.BaseUpdateTemplateOrIsoPermissionsCmd;
import com.cloud.api.response.SuccessResponse;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateIsoPermissions", description = "Updates ISO permissions", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateIsoPermissionsCmd extends BaseUpdateTemplateOrIsoPermissionsCmd {
    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(UpdateIsoPermissionsCmd.class.getName());
    }

    @Override
    protected String getResponseName() {
        return "updateisopermissionsresponse";
    }

    @Override
    public long getEntityOwnerId() {
        final VirtualMachineTemplate template = _entityMgr.findById(VirtualMachineTemplate.class, getId());
        if (template != null) {
            return template.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
