package org.apache.cloudstack.api.command.admin.volume;

import com.cloud.storage.Snapshot;
import com.cloud.storage.Volume;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.volume.CreateVolumeCmd;
import org.apache.cloudstack.api.response.VolumeResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createVolume", responseObject = VolumeResponse.class, description = "Creates a disk volume from a disk offering. This disk volume must still be attached to a" +
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
