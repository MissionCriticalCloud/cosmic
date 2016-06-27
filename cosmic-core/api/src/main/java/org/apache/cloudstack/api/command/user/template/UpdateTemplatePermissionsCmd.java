package org.apache.cloudstack.api.command.user.template;

import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseUpdateTemplateOrIsoPermissionsCmd;
import org.apache.cloudstack.api.response.SuccessResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateTemplatePermissions", responseObject = SuccessResponse.class, description = "Updates a template visibility permissions. "
        + "A public template is visible to all accounts within the same domain. " + "A private template is visible only to the owner of the template. "
        + "A priviledged template is a private template with account permissions added. " + "Only accounts specified under the template permissions are visible to them.",
        entityType = {VirtualMachineTemplate.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateTemplatePermissionsCmd extends BaseUpdateTemplateOrIsoPermissionsCmd {
    @Override
    protected Logger getLogger() {
        return LoggerFactory.getLogger(UpdateTemplatePermissionsCmd.class.getName());
    }

    @Override
    protected String getResponseName() {
        return "updatetemplatepermissionsresponse";
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
