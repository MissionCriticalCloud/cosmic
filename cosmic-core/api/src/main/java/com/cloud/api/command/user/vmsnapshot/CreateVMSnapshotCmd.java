package com.cloud.api.command.user.vmsnapshot;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.response.VMSnapshotResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.uservm.UserVm;
import com.cloud.vm.snapshot.VMSnapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createVMSnapshot", group = APICommandGroup.SnapshotService, description = "Creates snapshot for a vm.", responseObject = VMSnapshotResponse.class, since = "4.2.0", entityType =
        {VMSnapshot.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateVMSnapshotCmd extends BaseAsyncCreateCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(CreateVMSnapshotCmd.class.getName());
    private static final String s_name = "createvmsnapshotresponse";

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.UUID, required = true, entityType = UserVmResponse.class, description = "The ID of the vm")
    private Long vmId;

    @Parameter(name = ApiConstants.VM_SNAPSHOT_DESCRIPTION, type = CommandType.STRING, description = "The description of the snapshot")
    private String description;

    @Parameter(name = ApiConstants.VM_SNAPSHOT_DISPLAYNAME, type = CommandType.STRING, description = "The display name of the snapshot")
    private String displayName;

    @Parameter(name = ApiConstants.VM_SNAPSHOT_QUIESCEVM, type = CommandType.BOOLEAN, description = "quiesce vm if true")
    private Boolean quiescevm;

    @Override
    public void create() throws ResourceAllocationException {
        final VMSnapshot vmsnapshot = _vmSnapshotService.allocVMSnapshot(getVmId(), getDisplayName(), getDescription());
        if (vmsnapshot != null) {
            setEntityId(vmsnapshot.getId());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create vm snapshot");
        }
    }

    public Long getVmId() {
        return vmId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_SNAPSHOT_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "creating snapshot for VM: " + getVmId();
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("VM Id: " + getVmId());
        final VMSnapshot result = _vmSnapshotService.createVMSnapshot(getVmId(), getEntityId(), isQuiescevm());
        if (result != null) {
            final VMSnapshotResponse response = _responseGenerator.createVMSnapshotResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create vm snapshot due to an internal error creating snapshot for vm " + getVmId());
        }
    }

    public boolean isQuiescevm() {
        return Boolean.TRUE.equals(quiescevm);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final UserVm userVM = _userVmService.getUserVm(vmId);
        return userVM.getAccountId();
    }
}
