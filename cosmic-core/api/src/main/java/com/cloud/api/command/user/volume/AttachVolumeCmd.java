package com.cloud.api.command.user.volume;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.model.enumeration.DiskControllerType;
import com.cloud.storage.Volume;
import com.cloud.user.Account;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "attachVolume", group = APICommandGroup.VolumeService, description = "Attaches a disk volume to a virtual machine.", responseObject = VolumeResponse.class, responseView =
        ResponseView.Restricted,
        entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AttachVolumeCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AttachVolumeCmd.class.getName());
    private static final String s_name = "attachvolumeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.DEVICE_ID, type = CommandType.LONG, description = "the ID of the device to map the volume to within the guest OS. "
            + "If no deviceId is passed in, the next available deviceId will be chosen. " + "Possible values for a Linux OS are:" + "* 0 - /dev/xvda" + "* 1 - /dev/xvdb" + "* 2 " +
            "- /dev/xvdc"
            + "* 4 - /dev/xvde" + "* 5 - /dev/xvdf" + "* 6 - /dev/xvdg" + "* 7 - /dev/xvdh" + "* 8 - /dev/xvdi" + "* 9 - /dev/xvdj")
    private Long deviceId;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VolumeResponse.class, required = true, description = "the ID of the disk volume")
    private Long id;

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.UUID, entityType = UserVmResponse.class,
            required = true, description = "    the ID of the virtual machine")
    private Long virtualMachineId;

    @Parameter(name = ApiConstants.DISK_CONTROLLER,
            required = false,
            type = CommandType.STRING,
            description = "the disk controller to use. Either 'IDE', 'VIRTIO' or 'SCSI'")
    private String diskController;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getDeviceId() {
        return deviceId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VOLUME_ATTACH;
    }

    @Override
    public String getEventDescription() {
        return "attaching volume: " + getId() + " to vm: " + getVirtualMachineId();
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Volume;
    }

    public Long getId() {
        return id;
    }

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Volume Id: " + getId() + " VmId: " + getVirtualMachineId());
        final Volume result = _volumeService.attachVolumeToVM(this);
        if (result != null) {
            final VolumeResponse response = _responseGenerator.createVolumeResponse(ResponseView.Restricted, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to attach volume");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Volume volume = _responseGenerator.findVolumeById(getId());
        if (volume == null) {
            return Account.ACCOUNT_ID_SYSTEM; // bad id given, parent this command to SYSTEM so ERROR events are tracked
        }
        return volume.getAccountId();
    }

    public DiskControllerType getDiskController() {
        if (diskController != null) {
            return DiskControllerType.valueOf(diskController);
        } else {
            return null;
        }
    }
}
