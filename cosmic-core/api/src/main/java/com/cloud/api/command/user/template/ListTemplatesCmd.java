package com.cloud.api.command.user.template;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListTaggedResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.TemplateResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.context.CallContext;
import com.cloud.legacymodel.storage.VirtualMachineTemplate;
import com.cloud.legacymodel.storage.VirtualMachineTemplate.TemplateFilter;
import com.cloud.legacymodel.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listTemplates", group = APICommandGroup.TemplateService, description = "List all public, private, and privileged templates.", responseObject = TemplateResponse.class, entityType =
        {VirtualMachineTemplate.class}, responseView = ResponseView.Restricted,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListTemplatesCmd extends BaseListTaggedResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListTemplatesCmd.class.getName());

    private static final String s_name = "listtemplatesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.HYPERVISOR, type = CommandType.STRING, description = "the hypervisor for which to restrict the search")
    private String hypervisor;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = TemplateResponse.class, description = "the template ID")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the template name")
    private String templateName;

    @Parameter(name = ApiConstants.TEMPLATE_FILTER,
            type = CommandType.STRING,
            required = true,
            description = "possible values are \"featured\", \"self\", \"selfexecutable\",\"sharedexecutable\",\"executable\", and \"community\". "
                    + "* featured : templates that have been marked as featured and public. "
                    + "* self : templates that have been registered or created by the calling user. "
                    + "* selfexecutable : same as self, but only returns templates that can be used to deploy a new VM. "
                    + "* sharedexecutable : templates ready to be deployed that have been granted to the calling user by another user. "
                    + "* executable : templates that are owned by the calling user, or public templates, that can be used to deploy a VM. "
                    + "* community : templates that have been marked as public but not featured. " + "* all : all templates (only usable by admins).")
    private String templateFilter;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "list templates by zoneId")
    private Long zoneId;

    @Parameter(name = ApiConstants.SHOW_REMOVED, type = CommandType.BOOLEAN, description = "show removed templates as well")
    private Boolean showRemoved;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getHypervisor() {
        return hypervisor;
    }

    public Long getId() {
        return id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public Boolean getShowRemoved() {
        return (showRemoved != null ? showRemoved : false);
    }

    public boolean listInReadyState() {

        final Account account = CallContext.current().getCallingAccount();
        // It is account specific if account is admin type and domainId and accountName are not null
        final boolean isAccountSpecific = (account == null || _accountService.isAdmin(account.getId())) && (getAccountName() != null) && (getDomainId() != null);
        // Show only those that are downloaded.
        final TemplateFilter templateFilter = TemplateFilter.valueOf(getTemplateFilter());
        final boolean onlyReady =
                (templateFilter == TemplateFilter.featured) || (templateFilter == TemplateFilter.selfexecutable) || (templateFilter == TemplateFilter.sharedexecutable) ||
                        (templateFilter == TemplateFilter.executable && isAccountSpecific) || (templateFilter == TemplateFilter.community);
        return onlyReady;
    }

    public String getTemplateFilter() {
        return templateFilter;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Template;
    }

    @Override
    public void execute() {
        final ListResponse<TemplateResponse> response = _queryService.listTemplates(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
