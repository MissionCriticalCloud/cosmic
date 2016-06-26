package org.apache.cloudstack.api.command.user.vmsnapshot;

import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.uservm.UserVm;
import com.cloud.vm.snapshot.VMSnapshot;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.api.response.VMSnapshotResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createVMSnapshot", description = "Creates snapshot for a vm.", responseObject = VMSnapshotResponse.class, since = "4.2.0", entityType = {VMSnapshot.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateVMSnapshotCmd extends BaseAsyncCreateCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(CreateVMSnapshotCmd.class.getName());
    private static final String s_name = "createvmsnapshotresponse";

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.UUID, required = true, entityType = UserVmResponse.class, description = "The ID of the vm")
    private Long vmId;

    @Parameter(name = ApiConstants.VM_SNAPSHOT_DESCRIPTION, type = CommandType.STRING, required = false, description = "The description of the snapshot")
    private String description;

    @Parameter(name = ApiConstants.VM_SNAPSHOT_DISPLAYNAME, type = CommandType.STRING, required = false, description = "The display name of the snapshot")
    private String displayName;

    @Parameter(name = ApiConstants.VM_SNAPSHOT_MEMORY, type = CommandType.BOOLEAN, required = false, description = "snapshot memory if true")
    private Boolean snapshotMemory;

    @Parameter(name = ApiConstants.VM_SNAPSHOT_QUIESCEVM, type = CommandType.BOOLEAN, required = false, description = "quiesce vm if true")
    private Boolean quiescevm;

    @Override
    public void create() throws ResourceAllocationException {
        final VMSnapshot vmsnapshot = _vmSnapshotService.allocVMSnapshot(getVmId(), getDisplayName(), getDescription(), snapshotMemory());
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

    public Boolean snapshotMemory() {
        if (snapshotMemory == null) {
            return false;
        } else {
            return snapshotMemory;
        }
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
        final VMSnapshot result = _vmSnapshotService.creatVMSnapshot(getVmId(), getEntityId(), getQuiescevm());
        if (result != null) {
            final VMSnapshotResponse response = _responseGenerator.createVMSnapshotResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create vm snapshot due to an internal error creating snapshot for vm " + getVmId());
        }
    }

    public Boolean getQuiescevm() {
        if (quiescevm == null) {
            return false;
        } else {
            return quiescevm;
        }
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
