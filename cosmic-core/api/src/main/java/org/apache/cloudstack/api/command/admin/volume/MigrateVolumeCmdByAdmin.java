package org.apache.cloudstack.api.command.admin.volume;

import com.cloud.storage.Volume;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.volume.MigrateVolumeCmd;
import org.apache.cloudstack.api.response.VolumeResponse;

@APICommand(name = "migrateVolume", description = "Migrate volume", responseObject = VolumeResponse.class, since = "3.0.0", responseView = ResponseView.Full, entityType =
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
