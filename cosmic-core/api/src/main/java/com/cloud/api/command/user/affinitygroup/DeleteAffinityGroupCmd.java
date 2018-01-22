package com.cloud.api.command.user.affinitygroup;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.affinity.AffinityGroup;
import com.cloud.affinity.AffinityGroupResponse;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteAffinityGroup", group = APICommandGroup.AffinityGroupService, description = "Deletes affinity group", responseObject = SuccessResponse.class, entityType = {AffinityGroup.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteAffinityGroupCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteAffinityGroupCmd.class.getName());
    private static final String s_name = "deleteaffinitygroupresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "the account of the affinity group. Must be specified with domain ID")
    private String accountName;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            description = "the domain ID of account owning the affinity group",
            entityType = DomainResponse.class)
    private Long domainId;

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            description = "The ID of the affinity group. Mutually exclusive with name parameter",
            entityType = AffinityGroupResponse.class)
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "The name of the affinity group. Mutually exclusive with ID parameter")
    private String name;

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, description = "the project of the affinity group", entityType = ProjectResponse.class)
    private Long projectId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getId() {
        return id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final boolean result = _affinityGroupService.deleteAffinityGroup(id, accountName, projectId, domainId, name);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete affinity group");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Account caller = CallContext.current().getCallingAccount();

        //For domain wide affinity groups (if the affinity group processor type allows it)
        if (projectId == null && domainId != null && accountName == null && _accountService.isRootAdmin(caller.getId())) {
            return Account.ACCOUNT_ID_SYSTEM;
        }
        final Account owner = _accountService.finalizeOwner(caller, accountName, domainId, projectId);
        if (owner == null) {
            return caller.getAccountId();
        }
        return owner.getAccountId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_AFFINITY_GROUP_DELETE;
    }

    @Override
    public String getEventDescription() {
        return "Deleting Affinity Group";
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.AffinityGroup;
    }
}
