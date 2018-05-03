package com.cloud.api.command.admin.volume;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.volume.CreateVolumeCmd;
import com.cloud.api.response.VolumeResponse;
import com.cloud.context.CallContext;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.storage.Snapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createVolume", group = APICommandGroup.VolumeService, responseObject = VolumeResponse.class, description = "Creates a disk volume from a disk offering. This disk volume must " +
        "still be attached to a" +
        " virtual machine to make use of it.", responseView = ResponseView.Full, entityType = {
        Volume.class, VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateVolumeCmdByAdmin extends CreateVolumeCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateVolumeCmdByAdmin.class.getName());

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Volume Id: " + getEntityId() + ((getSnapshotId() == null) ? "" : " from snapshot: " + getSnapshotId()));
        final Volume volume = _volumeService.createVolume(this);
        if (volume != null) {
            final VolumeResponse response = _responseGenerator.createVolumeResponse(ResponseView.Full, volume);
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
}
