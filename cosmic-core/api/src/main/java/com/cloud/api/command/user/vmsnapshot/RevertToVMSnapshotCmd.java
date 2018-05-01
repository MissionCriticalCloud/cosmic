package com.cloud.api.command.user.vmsnapshot;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.response.VMSnapshotResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.vm.snapshot.VMSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "revertToVMSnapshot", group = APICommandGroup.SnapshotService, description = "Revert VM from a vmsnapshot.", responseObject = UserVmResponse.class, since = "4.2.0", responseView
        = ResponseView
        .Restricted,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class RevertToVMSnapshotCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(RevertToVMSnapshotCmd.class.getName());
    private static final String s_name = "reverttovmsnapshotresponse";

    @ACL(accessType = AccessType.OperateEntry, pointerToEntity = "getVmId()")
    @Parameter(name = ApiConstants.VM_SNAPSHOT_ID,
            type = CommandType.UUID,
            required = true,
            entityType = VMSnapshotResponse.class,
            description = "The ID of the vm snapshot")
    private Long vmSnapShotId;

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ResourceAllocationException, ConcurrentOperationException {
        CallContext.current().setEventDetails("vmsnapshot id: " + getVmSnapShotId());
        final UserVm result = _vmSnapshotService.revertToSnapshot(getVmSnapShotId());
        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Restricted,
                    "virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to revert VM snapshot");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final VMSnapshot vmSnapshot = _entityMgr.findById(VMSnapshot.class, getVmSnapShotId());
        if (vmSnapshot != null) {
            return vmSnapshot.getAccountId();
        }
        return Account.ACCOUNT_ID_SYSTEM;
    }

    public Long getVmSnapShotId() {
        return vmSnapShotId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_SNAPSHOT_REVERT;
    }

    @Override
    public String getEventDescription() {
        return "Revert from VM snapshot: " + getVmSnapShotId();
    }
}
