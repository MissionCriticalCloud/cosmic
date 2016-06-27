package org.apache.cloudstack.api.command.admin.volume;

import com.cloud.storage.Volume;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.volume.UpdateVolumeCmd;
import org.apache.cloudstack.api.response.VolumeResponse;
import org.apache.cloudstack.context.CallContext;

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
