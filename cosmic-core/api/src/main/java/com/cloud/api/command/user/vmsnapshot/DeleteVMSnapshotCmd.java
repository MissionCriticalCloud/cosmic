package com.cloud.api.command.user.vmsnapshot;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SuccessResponse;
import com.cloud.api.response.VMSnapshotResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.storage.VMSnapshot;
import com.cloud.legacymodel.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteVMSnapshot", group = APICommandGroup.SnapshotService, description = "Deletes a vmsnapshot.", responseObject = SuccessResponse.class, since = "4.2.0", entityType =
        {VMSnapshot.class},
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
