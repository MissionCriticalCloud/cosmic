package com.cloud.api.command.user.vmgroup;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.InstanceGroupResponse;
import com.cloud.user.Account;
import com.cloud.vm.InstanceGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateInstanceGroup", description = "Updates a vm group", responseObject = InstanceGroupResponse.class, entityType = {InstanceGroup.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateVMGroupCmd extends BaseCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(UpdateVMGroupCmd.class.getName());
    private static final String s_name = "updateinstancegroupresponse";
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = InstanceGroupResponse.class, required = true, description = "Instance group ID")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "new instance group name")
    private String groupName;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getGroupName() {
        return groupName;
    }

    @Override
    public void execute() {
        final InstanceGroup result = _mgr.updateVmGroup(this);
        if (result != null) {
            final InstanceGroupResponse response = _responseGenerator.createInstanceGroupResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update vm instance group");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final InstanceGroup group = _entityMgr.findById(InstanceGroup.class, getId());
        if (group != null) {
            return group.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    public Long getId() {
        return id;
    }
}
