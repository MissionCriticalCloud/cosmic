package com.cloud.api.command.admin.volume;

import com.cloud.api.APICommand;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.volume.UpdateVolumeCmd;
import com.cloud.api.response.VolumeResponse;
import com.cloud.context.CallContext;
import com.cloud.storage.Volume;

@APICommand(name = "updateVolume", description = "Updates the volume.", responseObject = VolumeResponse.class, responseView = ResponseView.Full, entityType = {Volume.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateVolumeCmdByAdmin extends UpdateVolumeCmd {

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Volume Id: " + getId());
        final Volume result = _volumeService.updateVolume(getId(), getPath(), getState(), getStorageId(), getDisplayVolume(),
                getCustomId(), getEntityOwnerId(), getChainInfo());
        if (result != null) {
            final VolumeResponse response = _responseGenerator.createVolumeResponse(ResponseView.Full, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update volume");
        }
    }
}
