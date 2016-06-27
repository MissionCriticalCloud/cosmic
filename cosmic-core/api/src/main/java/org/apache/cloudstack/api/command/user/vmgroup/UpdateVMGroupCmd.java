package org.apache.cloudstack.api.command.user.vmgroup;

import com.cloud.user.Account;
import com.cloud.vm.InstanceGroup;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.InstanceGroupResponse;

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
