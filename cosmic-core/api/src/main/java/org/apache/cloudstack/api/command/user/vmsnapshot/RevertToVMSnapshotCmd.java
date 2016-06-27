package org.apache.cloudstack.api.command.user.vmsnapshot;

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.vm.snapshot.VMSnapshot;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.api.response.VMSnapshotResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "revertToVMSnapshot", description = "Revert VM from a vmsnapshot.", responseObject = UserVmResponse.class, since = "4.2.0", responseView = ResponseView
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
