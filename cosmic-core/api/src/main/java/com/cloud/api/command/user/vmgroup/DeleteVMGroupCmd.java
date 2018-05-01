package com.cloud.api.command.user.vmgroup;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.InstanceGroupResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.legacymodel.user.Account;
import com.cloud.vm.InstanceGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteInstanceGroup", group = APICommandGroup.VMGroupService, description = "Deletes a vm group", responseObject = SuccessResponse.class, entityType = {InstanceGroup.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteVMGroupCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteVMGroupCmd.class.getName());
    private static final String s_name = "deleteinstancegroupresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = InstanceGroupResponse.class, required = true, description = "the ID of the instance group")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final boolean result = _userVmService.deleteVmGroup(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete vm group");
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
