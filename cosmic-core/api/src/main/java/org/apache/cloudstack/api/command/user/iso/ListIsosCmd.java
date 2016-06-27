package org.apache.cloudstack.api.command.user.iso;

import com.cloud.template.VirtualMachineTemplate.TemplateFilter;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListTaggedResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.TemplateResponse;
import org.apache.cloudstack.api.response.ZoneResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listIsos", description = "Lists all available ISO files.", responseObject = TemplateResponse.class, responseView = ResponseView.Restricted,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListIsosCmd extends BaseListTaggedResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListIsosCmd.class.getName());

    private static final String s_name = "listisosresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.BOOTABLE, type = CommandType.BOOLEAN, description = "true if the ISO is bootable, false otherwise")
    private Boolean bootable;

    @Parameter(name = ApiConstants.HYPERVISOR, type = CommandType.STRING, description = "the hypervisor for which to restrict the search")
    private String hypervisor;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = TemplateResponse.class, description = "list ISO by ID")
    private Long id;

    @Parameter(name = ApiConstants.IS_PUBLIC, type = CommandType.BOOLEAN, description = "true if the ISO is publicly available to all users, false otherwise.")
    private Boolean publicIso;

    @Parameter(name = ApiConstants.IS_READY, type = CommandType.BOOLEAN, description = "true if this ISO is ready to be deployed")
    private Boolean ready;

    @Parameter(name = ApiConstants.ISO_FILTER,
            type = CommandType.STRING,
            description = "possible values are \"featured\", \"self\", \"selfexecutable\",\"sharedexecutable\",\"executable\", and \"community\". "
                    + "* featured : templates that have been marked as featured and public. "
                    + "* self : templates that have been registered or created by the calling user. "
                    + "* selfexecutable : same as self, but only returns templates that can be used to deploy a new VM. "
                    + "* sharedexecutable : templates ready to be deployed that have been granted to the calling user by another user. "
                    + "* executable : templates that are owned by the calling user, or public templates, that can be used to deploy a VM. "
                    + "* community : templates that have been marked as public but not featured. " + "* all : all templates (only usable by admins).")
    private final String isoFilter = TemplateFilter.selfexecutable.toString();

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "list all ISOs by name")
    private String isoName;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the ID of the zone")
    private Long zoneId;

    @Parameter(name = ApiConstants.SHOW_REMOVED, type = CommandType.BOOLEAN, description = "show removed ISOs as well")
    private Boolean showRemoved;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Boolean isBootable() {
        return bootable;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public Long getId() {
        return id;
    }

    public Boolean isPublic() {
        return publicIso;
    }

    public String getIsoName() {
        return isoName;
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
        final TemplateFilter templateFilter = TemplateFilter.valueOf(getIsoFilter());
        boolean onlyReady =
                (templateFilter == TemplateFilter.featured) || (templateFilter == TemplateFilter.selfexecutable) || (templateFilter == TemplateFilter.sharedexecutable) ||
                        (templateFilter == TemplateFilter.executable && isAccountSpecific) || (templateFilter == TemplateFilter.community);

        if (!onlyReady) {
            if (isReady() != null && isReady().booleanValue() != onlyReady) {
                onlyReady = isReady().booleanValue();
            }
        }

        return onlyReady;
    }

    public String getIsoFilter() {
        return isoFilter;
    }

    public Boolean isReady() {
        return ready;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Iso;
    }

    @Override
    public void execute() {
        final ListResponse<TemplateResponse> response = _queryService.listIsos(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
