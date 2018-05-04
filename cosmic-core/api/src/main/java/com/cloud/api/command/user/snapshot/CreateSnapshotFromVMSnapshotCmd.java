package com.cloud.api.command.user.snapshot;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SnapshotResponse;
import com.cloud.api.response.VMSnapshotResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.PermissionDeniedException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.storage.VMSnapshot;
import com.cloud.legacymodel.user.Account;
import com.cloud.projects.Project;
import com.cloud.storage.Snapshot;
import com.cloud.uservm.UserVm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createSnapshotFromVMSnapshot", group = APICommandGroup.SnapshotService, description = "Creates an instant snapshot of a volume from existing vm snapshot.", responseObject =
        SnapshotResponse.class,
        entityType = {Snapshot.class}, requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateSnapshotFromVMSnapshotCmd extends BaseAsyncCreateCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(CreateSnapshotFromVMSnapshotCmd.class.getName());
    private static final String s_name = "createsnapshotfromvmsnapshotresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.VOLUME_ID, type = CommandType.UUID, entityType = VolumeResponse.class, required = true, description = "The ID of the disk volume")
    private Long volumeId;

    @Parameter(name = ApiConstants.VM_SNAPSHOT_ID, type = CommandType.UUID, entityType = VMSnapshotResponse.class, required = true, description = "The ID of the VM snapshot")
    private Long vmSnapshotId;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name of the snapshot")
    private String snapshotName;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getVolumeId() {
        return volumeId;
    }

    public Long getVMSnapshotId() {
        return vmSnapshotId;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    private Long getVmId() {
        final VMSnapshot vmsnapshot = _entityMgr.findById(VMSnapshot.class, getVMSnapshotId());
        if (vmsnapshot == null) {
            throw new InvalidParameterValueException("Unable to find vm snapshot by id=" + getVMSnapshotId());
        }
        final UserVm vm = _entityMgr.findById(UserVm.class, vmsnapshot.getVmId());
        if (vm == null) {
            throw new InvalidParameterValueException("Unable to find vm by vm snapshot id=" + getVMSnapshotId());
        }
        return vm.getId();
    }

    private Long getHostId() {
        final VMSnapshot vmsnapshot = _entityMgr.findById(VMSnapshot.class, getVMSnapshotId());
        if (vmsnapshot == null) {
            throw new InvalidParameterValueException("Unable to find vm snapshot by id=" + getVMSnapshotId());
        }
        final UserVm vm = _entityMgr.findById(UserVm.class, vmsnapshot.getVmId());
        if (vm != null) {
            if (vm.getHostId() != null) {
                return vm.getHostId();
            } else if (vm.getLastHostId() != null) {
                return vm.getLastHostId();
            }
        }
        return null;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    public static String getResultObjectName() {
        return ApiConstants.SNAPSHOT;
    }

    @Override
    public long getEntityOwnerId() {
        final VMSnapshot vmsnapshot = _entityMgr.findById(VMSnapshot.class, getVMSnapshotId());
        if (vmsnapshot == null) {
            throw new InvalidParameterValueException("Unable to find vmsnapshot by id=" + getVMSnapshotId());
        }

        final Account account = _accountService.getAccount(vmsnapshot.getAccountId());
        //Can create templates for enabled projects/accounts only
        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            final Project project = _projectService.findByProjectAccountId(vmsnapshot.getAccountId());
            if (project == null) {
                throw new InvalidParameterValueException("Unable to find project by account id=" + account.getUuid());
            }
            if (project.getState() != Project.State.Active) {
                throw new PermissionDeniedException("Can't add resources to the project id=" + project.getUuid() + " in state=" + project.getState() + " as it's no longer active");
            }
        } else if (account.getState() == Account.State.disabled) {
            throw new PermissionDeniedException("The owner of template is disabled: " + account);
        }

        return vmsnapshot.getAccountId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_SNAPSHOT_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "creating snapshot from vm snapshot : " + getVMSnapshotId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Snapshot;
    }

    @Override
    public void create() throws ResourceAllocationException {
        final Snapshot snapshot = this._volumeService.allocSnapshotForVm(getVmId(), getVolumeId(), getSnapshotName());
        if (snapshot != null) {
            this.setEntityId(snapshot.getId());
            this.setEntityUuid(snapshot.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create snapshot from vm snapshot");
        }
    }

    @Override
    public void execute() {
        s_logger.info("CreateSnapshotFromVMSnapshotCmd with vm snapshot id:" + getVMSnapshotId() + " and snapshot id:" + getEntityId() + " starts:" + System.currentTimeMillis());
        CallContext.current().setEventDetails("Vm Snapshot Id: " + getVMSnapshotId());
        Snapshot snapshot = null;
        try {
            snapshot = _snapshotService.backupSnapshotFromVmSnapshot(getEntityId(), getVmId(), getVolumeId(), getVMSnapshotId());
            if (snapshot != null) {
                SnapshotResponse response = _responseGenerator.createSnapshotResponse(snapshot);
                response.setResponseName(getCommandName());
                this.setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create snapshot due to an internal error creating snapshot from vm snapshot " + getVMSnapshotId());
            }
        } catch (final InvalidParameterValueException ex) {
            throw ex;
        } catch (final Exception e) {
            s_logger.debug("Failed to create snapshot", e);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create snapshot due to an internal error creating snapshot from vm snapshot " + getVMSnapshotId());
        } finally {
            if (snapshot == null) {
                try {
                    _snapshotService.deleteSnapshot(getEntityId());
                } catch (final Exception e) {
                    s_logger.debug("Failed to clean failed snapshot" + getEntityId());
                }
            }
        }
    }

    @Override
    public String getSyncObjType() {
        if (getSyncObjId() != null) {
            return BaseAsyncCmd.snapshotHostSyncObject;
        }
        return null;
    }

    @Override
    public Long getSyncObjId() {
        if (getHostId() != null) {
            return getHostId();
        }
        return null;
    }
}
