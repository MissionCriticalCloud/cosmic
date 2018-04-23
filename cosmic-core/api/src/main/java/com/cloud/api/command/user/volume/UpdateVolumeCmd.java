package com.cloud.api.command.user.volume;

import com.cloud.acl.RoleType;
import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCustomIdCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.storage.Volume;
import com.cloud.utils.exception.InvalidParameterValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateVolume", group = APICommandGroup.VolumeService, description = "Updates the volume.", responseObject = VolumeResponse.class, responseView = ResponseView.Restricted,
        entityType = {Volume.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateVolumeCmd extends BaseAsyncCustomIdCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateVolumeCmd.class.getName());
    private static final String s_name = "updatevolumeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VolumeResponse.class, description = "the ID of the disk volume")
    private Long id;

    @Parameter(name = ApiConstants.PATH, type = CommandType.STRING, description = "The path of the volume")
    private String path;

    @Parameter(name = ApiConstants.CHAIN_INFO,
            type = CommandType.STRING,
            description = "The chain info of the volume",
            since = "4.4")
    private String chainInfo;

    @Parameter(name = ApiConstants.STORAGE_ID,
            type = CommandType.UUID,
            entityType = StoragePoolResponse.class,
            description = "Destination storage pool UUID for the volume",
            since = "4.3")
    private Long storageId;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "The state of the volume", since = "4.3")
    private String state;

    @Parameter(name = ApiConstants.DISPLAY_VOLUME,
            type = CommandType.BOOLEAN,
            description = "an optional field, whether to the display the volume to the end user or not.", authorized = {RoleType.Admin})
    private Boolean displayVolume;

    @Parameter(name = ApiConstants.DISK_CONTROLLER,
            required = false,
            type = CommandType.STRING,
            description = "the disk controller to use. Either 'IDE', 'VIRTIO' or 'SCSI'")
    private String diskController;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VOLUME_UPDATE;
    }

    @Override
    public String getEventDescription() {
        final StringBuilder desc = new StringBuilder("Updating volume: ");
        desc.append(getId()).append(" with");
        if (getPath() != null) {
            desc.append(" path " + getPath());
        }
        if (getStorageId() != null) {
            desc.append(", storage id " + getStorageId());
        }

        if (getState() != null) {
            desc.append(", state " + getState());
        }
        return desc.toString();
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Volume;
    }

    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }
    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getStorageId() {
        return storageId;
    }

    public String getState() {
        return state;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Volume Id: " + getId());
        final Volume result = _volumeService.updateVolume(getId(), getPath(), getState(), getStorageId(), getDisplayVolume(),
                getCustomId(), getEntityOwnerId(), getChainInfo(), getDiskController());
        if (result != null) {
            final VolumeResponse response = _responseGenerator.createVolumeResponse(ResponseView.Restricted, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update volume");
        }
    }

    public Boolean getDisplayVolume() {
        return displayVolume;
    }

    public String getChainInfo() {
        return chainInfo;
    }

    public DiskControllerType getDiskController() {
        if (diskController != null) {
            return DiskControllerType.valueOf(diskController);
        } else {
            return null;
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Volume volume = _responseGenerator.findVolumeById(getId());
        if (volume == null) {
            throw new InvalidParameterValueException("Invalid volume id was provided");
        }
        return volume.getAccountId();
    }

    @Override
    public void checkUuid() {
        if (getCustomId() != null) {
            _uuidMgr.checkUuid(getCustomId(), Volume.class);
        }
    }
}
