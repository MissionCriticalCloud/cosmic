package com.cloud.api.command.user.volume;

import com.cloud.acl.RoleType;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCreateCustomIdCmd;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DiskOfferingResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.SnapshotResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.storage.Snapshot;
import com.cloud.storage.Volume;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createVolume", group = APICommandGroup.VolumeService, responseObject = VolumeResponse.class, description = "Creates a disk volume from a disk offering. This disk volume must " +
        "still be attached to a" +
        " virtual machine to make use of it.", responseView = ResponseView.Restricted, entityType = {
        Volume.class, VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateVolumeCmd extends BaseAsyncCreateCustomIdCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateVolumeCmd.class.getName());
    private static final String s_name = "createvolumeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ACCOUNT,
            type = BaseCmd.CommandType.STRING,
            description = "the account associated with the disk volume. Must be used with the domainId parameter.")
    private String accountName;

    @Parameter(name = ApiConstants.PROJECT_ID,
            type = CommandType.UUID,
            entityType = ProjectResponse.class,
            description = "the project associated with the volume. Mutually exclusive with account parameter")
    private Long projectId;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            description = "the domain ID associated with the disk offering. If used with the account parameter"
                    + " returns the disk volume associated with the account for the specified domain.")
    private Long domainId;

    @Parameter(name = ApiConstants.DISK_OFFERING_ID,
            required = false,
            type = CommandType.UUID,
            entityType = DiskOfferingResponse.class,
            description = "the ID of the disk offering. Either diskOfferingId or snapshotId must be passed in.")
    private Long diskOfferingId;

    @Parameter(name = ApiConstants.DISK_CONTROLLER,
            required = false,
            type = CommandType.STRING,
            description = "the disk controller to use. Either 'IDE', 'VIRTIO' or 'SCSI'")
    private String diskController;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name of the disk volume")
    private String volumeName;

    @Parameter(name = ApiConstants.SIZE, type = CommandType.LONG, description = "Arbitrary volume size")
    private Long size;

    @Parameter(name = ApiConstants.MIN_IOPS, type = CommandType.LONG, description = "min iops")
    private Long minIops;

    @Parameter(name = ApiConstants.MAX_IOPS, type = CommandType.LONG, description = "max iops")
    private Long maxIops;

    @Parameter(name = ApiConstants.SNAPSHOT_ID,
            type = CommandType.UUID,
            entityType = SnapshotResponse.class,
            description = "the snapshot ID for the disk volume. Either diskOfferingId or snapshotId must be passed in.")
    private Long snapshotId;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the ID of the availability zone")
    private Long zoneId;

    @Parameter(name = ApiConstants.DISPLAY_VOLUME, type = CommandType.BOOLEAN, description = "an optional field, whether to display the volume to the end user or not.",
            authorized = {RoleType.Admin})
    private Boolean displayVolume;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID,
            type = CommandType.UUID,
            entityType = UserVmResponse.class,
            description = "the ID of the virtual machine; to be used with snapshot Id, VM to which the volume gets attached after creation")
    private Long virtualMachineId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "volume";
    }

    public String getAccountName() {
        return accountName;
    }

    public Long getDiskOfferingId() {
        return diskOfferingId;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getSize() {
        return size;
    }

    public Long getMinIops() {
        return minIops;
    }

    public Long getMaxIops() {
        return maxIops;
    }

    public Long getZoneId() {
        return zoneId;
    }

    private Long getProjectId() {
        return projectId;
    }

    public Boolean getDisplayVolume() {
        return displayVolume;
    }

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VOLUME_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "creating volume: " + getVolumeName() + ((getSnapshotId() == null) ? "" : " from snapshot: " + getSnapshotId());
    }

    public String getVolumeName() {
        return volumeName;
    }

    public Long getSnapshotId() {
        return snapshotId;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Volume;
    }

    @Override
    public void create() throws ResourceAllocationException {

        final Volume volume = _volumeService.allocVolume(this);
        if (volume != null) {
            setEntityId(volume.getId());
            setEntityUuid(volume.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create volume");
        }
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Volume Id: " + getEntityId() + ((getSnapshotId() == null) ? "" : " from snapshot: " + getSnapshotId()));
        final Volume volume = _volumeService.createVolume(this);
        if (volume != null) {
            final VolumeResponse response = _responseGenerator.createVolumeResponse(ResponseView.Restricted, volume);
            //FIXME - have to be moved to ApiResponseHelper
            if (getSnapshotId() != null) {
                final Snapshot snap = _entityMgr.findById(Snapshot.class, getSnapshotId());
                if (snap != null) {
                    response.setSnapshotId(snap.getUuid()); // if the volume was
                    // created from a
                    // snapshot,
                    // snapshotId will
                    // be set so we pass
                    // it back in the
                    // response
                }
            }
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create a volume");
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
        final Long accountId = _accountService.finalyzeAccountId(accountName, domainId, projectId, true);
        if (accountId == null) {
            return CallContext.current().getCallingAccount().getId();
        }

        return accountId;
    }

    @Override
    public boolean isDisplay() {
        if (displayVolume == null) {
            return true;
        } else {
            return displayVolume;
        }
    }

    public String getDiskController() {
        return diskController;
    }
}
