package org.apache.cloudstack.api.command.user.vmsnapshot;

import com.cloud.event.EventTypes;
import com.cloud.user.Account;
import com.cloud.vm.snapshot.VMSnapshot;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.api.response.VMSnapshotResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteVMSnapshot", description = "Deletes a vmsnapshot.", responseObject = SuccessResponse.class, since = "4.2.0", entityType = {VMSnapshot.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteVMSnapshotCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteVMSnapshotCmd.class.getName());
    private static final String s_name = "deletevmsnapshotresponse";

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.VM_SNAPSHOT_ID,
            type = CommandType.UUID,
            entityType = VMSnapshotResponse.class,
            required = true,
            description = "The ID of the VM snapshot")
    private Long id;

    @Override
    public void execute() {
        CallContext.current().setEventDetails("vmsnapshot id: " + getId());
        final boolean result = _vmSnapshotService.deleteVMSnapshot(getId());
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete vm snapshot");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final VMSnapshot vmSnapshot = _entityMgr.findById(VMSnapshot.class, getId());
        if (vmSnapshot != null) {
            return vmSnapshot.getAccountId();
        }
        return Account.ACCOUNT_ID_SYSTEM;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_SNAPSHOT_DELETE;
    }

    @Override
    public String getEventDescription() {
        return "Delete VM snapshot: " + getId();
    }
}
