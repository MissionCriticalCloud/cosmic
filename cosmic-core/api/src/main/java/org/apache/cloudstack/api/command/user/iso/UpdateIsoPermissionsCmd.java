package org.apache.cloudstack.api.command.user.iso;

import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseUpdateTemplateOrIsoPermissionsCmd;
import org.apache.cloudstack.api.response.SuccessResponse;

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
