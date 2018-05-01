package com.cloud.api.command.user.template;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.BaseUpdateTemplateOrIsoPermissionsCmd;
import com.cloud.api.response.SuccessResponse;
import com.cloud.legacymodel.user.Account;
import com.cloud.template.VirtualMachineTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateTemplatePermissions", group = APICommandGroup.TemplateService, responseObject = SuccessResponse.class, description = "Updates a template visibility permissions. "
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
