package org.apache.cloudstack.api.command.admin.volume;

import com.cloud.storage.Volume;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.volume.DetachVolumeCmd;
import org.apache.cloudstack.api.response.VolumeResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "detachVolume", description = "Detaches a disk volume from a virtual machine.", responseObject = VolumeResponse.class, responseView = ResponseView.Full,
        entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DetachVolumeCmdByAdmin extends DetachVolumeCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DetachVolumeCmdByAdmin.class.getName());

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Volume Id: " + getId() + " VmId: " + getVirtualMachineId());
        final Volume result = _volumeService.detachVolumeFromVM(this);
        if (result != null) {
            final VolumeResponse response = _responseGenerator.createVolumeResponse(ResponseView.Full, result);
            response.setResponseName("volume");
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to detach volume");
        }
    }
}
