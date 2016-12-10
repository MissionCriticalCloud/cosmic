package com.cloud.api.command.admin.volume;

import com.cloud.api.APICommand;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.volume.AttachVolumeCmd;
import com.cloud.api.response.VolumeResponse;
import com.cloud.context.CallContext;
import com.cloud.storage.Volume;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "attachVolume", description = "Attaches a disk volume to a virtual machine.", responseObject = VolumeResponse.class, responseView = ResponseView.Full,
        entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AttachVolumeCmdByAdmin extends AttachVolumeCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AttachVolumeCmdByAdmin.class.getName());

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Volume Id: " + getId() + " VmId: " + getVirtualMachineId());
        final Volume result = _volumeService.attachVolumeToVM(this);
        if (result != null) {
            final VolumeResponse response = _responseGenerator.createVolumeResponse(ResponseView.Full, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to attach volume");
        }
    }
}
