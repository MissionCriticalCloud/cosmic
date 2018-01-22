package com.cloud.api.command.admin.volume;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.volume.ResizeVolumeCmd;
import com.cloud.api.response.VolumeResponse;
import com.cloud.context.CallContext;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.storage.Volume;
import com.cloud.utils.exception.InvalidParameterValueException;

@APICommand(name = "resizeVolume", group = APICommandGroup.VolumeService, description = "Resizes a volume", responseObject = VolumeResponse.class, responseView = ResponseView.Full, entityType = {Volume.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ResizeVolumeCmdByAdmin extends ResizeVolumeCmd {

    @Override
    public void execute() throws ResourceAllocationException {
        Volume volume = null;
        try {
            CallContext.current().setEventDetails("Volume Id: " + getEntityId() + " to size " + getSize() + "G");
            volume = _volumeService.resizeVolume(this);
        } catch (final InvalidParameterValueException ex) {
            s_logger.info(ex.getMessage());
            throw new ServerApiException(ApiErrorCode.UNSUPPORTED_ACTION_ERROR, ex.getMessage());
        }

        if (volume != null) {
            final VolumeResponse response = _responseGenerator.createVolumeResponse(ResponseView.Full, volume);
            //FIXME - have to be moved to ApiResponseHelper
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to resize volume");
        }
    }
}
