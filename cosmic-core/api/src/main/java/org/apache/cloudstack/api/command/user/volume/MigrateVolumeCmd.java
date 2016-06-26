package org.apache.cloudstack.api.command.user.volume;

import com.cloud.event.EventTypes;
import com.cloud.storage.Volume;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.StoragePoolResponse;
import org.apache.cloudstack.api.response.VolumeResponse;

@APICommand(name = "migrateVolume", description = "Migrate volume", responseObject = VolumeResponse.class, since = "3.0.0", responseView = ResponseView.Restricted, entityType =
        {Volume.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class MigrateVolumeCmd extends BaseAsyncCmd {
    private static final String s_name = "migratevolumeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.VOLUME_ID, type = CommandType.UUID, entityType = VolumeResponse.class, required = true, description = "the ID of the volume")
    private Long volumeId;

    @Parameter(name = ApiConstants.STORAGE_ID,
            type = CommandType.UUID,
            entityType = StoragePoolResponse.class,
            required = true,
            description = "destination storage pool ID to migrate the volume to")
    private Long storageId;

    @Parameter(name = ApiConstants.LIVE_MIGRATE,
            type = CommandType.BOOLEAN,
            required = false,
            description = "if the volume should be live migrated when it is attached to a running vm")
    private Boolean liveMigrate;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return getVolumeId();
    }

    // TODO remove this in 5.0 and use id as param instead.
    public Long getVolumeId() {
        return volumeId;
    }

    public boolean isLiveMigrate() {
        return (liveMigrate != null) ? liveMigrate : false;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VOLUME_MIGRATE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "Attempting to migrate volume Id: " + getVolumeId() + " to storage pool Id: " + getStoragePoolId();
    }

    public Long getStoragePoolId() {
        return storageId;
    }

    @Override
    public void execute() {
        final Volume result;

        result = _volumeService.migrateVolume(this);
        if (result != null) {
            final VolumeResponse response = _responseGenerator.createVolumeResponse(ResponseView.Restricted, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to migrate volume");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Volume volume = _entityMgr.findById(Volume.class, getVolumeId());
        if (volume != null) {
            return volume.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
