package org.apache.cloudstack.api.command.admin.volume;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.storage.Volume;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.volume.ResizeVolumeCmd;
import org.apache.cloudstack.api.response.VolumeResponse;
import org.apache.cloudstack.context.CallContext;

@APICommand(name = "resizeVolume", description = "Resizes a volume", responseObject = VolumeResponse.class, responseView = ResponseView.Full, entityType = {Volume.class},
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
