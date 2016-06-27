package org.apache.cloudstack.api.command.admin.vmsnapshot;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.uservm.UserVm;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.vmsnapshot.RevertToVMSnapshotCmd;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "revertToVMSnapshot", description = "Revert VM from a vmsnapshot.", responseObject = UserVmResponse.class, since = "4.2.0", responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class RevertToVMSnapshotCmdByAdmin extends RevertToVMSnapshotCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(RevertToVMSnapshotCmdByAdmin.class.getName());

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ResourceAllocationException, ConcurrentOperationException {
        CallContext.current().setEventDetails(
                "vmsnapshot id: " + getVmSnapShotId());
        final UserVm result = _vmSnapshotService.revertToSnapshot(getVmSnapShotId());
        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full,
                    "virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to revert VM snapshot");
        }
    }
}
