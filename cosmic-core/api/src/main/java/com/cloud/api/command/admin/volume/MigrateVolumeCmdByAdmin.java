package com.cloud.api.command.admin.volume;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.volume.MigrateVolumeCmd;
import com.cloud.api.response.VolumeResponse;
import com.cloud.storage.Volume;

@APICommand(name = "migrateVolume", group = APICommandGroup.VolumeService, description = "Migrate volume", responseObject = VolumeResponse.class, since = "3.0.0", responseView = ResponseView.Full, entityType =
        {Volume.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class MigrateVolumeCmdByAdmin extends MigrateVolumeCmd {

    @Override
    public void execute() {
        final Volume result;

        result = _volumeService.migrateVolume(this);
        if (result != null) {
            final VolumeResponse response = _responseGenerator.createVolumeResponse(ResponseView.Full, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to migrate volume");
        }
    }
}
