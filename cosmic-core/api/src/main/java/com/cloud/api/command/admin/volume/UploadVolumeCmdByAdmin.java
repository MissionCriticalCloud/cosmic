package com.cloud.api.command.admin.volume;

import com.cloud.api.APICommand;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.volume.UploadVolumeCmd;
import com.cloud.api.response.VolumeResponse;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.storage.Volume;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "uploadVolume", description = "Uploads a data disk.", responseObject = VolumeResponse.class, responseView = ResponseView.Full, entityType = {Volume.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UploadVolumeCmdByAdmin extends UploadVolumeCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UploadVolumeCmdByAdmin.class.getName());

    @Override
    public void execute() throws ResourceUnavailableException,
            InsufficientCapacityException, ServerApiException,
            ConcurrentOperationException, ResourceAllocationException,
            NetworkRuleConflictException {

        final Volume volume = _volumeService.uploadVolume(this);
        if (volume != null) {
            final VolumeResponse response = _responseGenerator.createVolumeResponse(ResponseView.Full, volume);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to upload volume");
        }
    }
}
