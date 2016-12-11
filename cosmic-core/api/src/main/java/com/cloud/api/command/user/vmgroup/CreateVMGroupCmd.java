package com.cloud.api.command.user.vmgroup;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.InstanceGroupResponse;
import com.cloud.api.response.ProjectResponse;
import com.cloud.context.CallContext;
import com.cloud.vm.InstanceGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createInstanceGroup", description = "Creates a vm group", responseObject = InstanceGroupResponse.class, entityType = {InstanceGroup.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateVMGroupCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateVMGroupCmd.class.getName());

    private static final String s_name = "createinstancegroupresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "the name of the instance group")
    private String groupName;

    @Parameter(name = ApiConstants.ACCOUNT,
            type = CommandType.STRING,
            description = "the account of the instance group. The account parameter must be used with the domainId parameter.")
    private String accountName;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            description = "the domain ID of account owning the instance group")
    private Long domainId;

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, description = "The project of the instance group")
    private Long projectId;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public String getGroupName() {
        return groupName;
    }

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getProjectId() {
        return projectId;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        final InstanceGroup result = _userVmService.createVmGroup(this);
        if (result != null) {
            final InstanceGroupResponse response = _responseGenerator.createInstanceGroupResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create vm instance group");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Long accountId = _accountService.finalyzeAccountId(accountName, domainId, projectId, true);
        if (accountId == null) {
            return CallContext.current().getCallingAccount().getId();
        }

        return accountId;
    }
}
