package org.apache.cloudstack.api.command.user.volume;

import com.cloud.event.EventTypes;
import com.cloud.storage.Volume;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.api.response.VolumeResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "detachVolume", description = "Detaches a disk volume from a virtual machine.", responseObject = VolumeResponse.class, responseView = ResponseView.Restricted,
        entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DetachVolumeCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DetachVolumeCmd.class.getName());
    private static final String s_name = "detachvolumeresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VolumeResponse.class,
            description = "the ID of the disk volume")
    private Long id;

    @Parameter(name = ApiConstants.DEVICE_ID, type = CommandType.LONG, description = "the device ID on the virtual machine where volume is detached from")
    private Long deviceId;

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID,
            type = CommandType.UUID,
            entityType = UserVmResponse.class,
            description = "the ID of the virtual machine where the volume is detached from")
    private Long virtualMachineId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "volume";
    }

    public Long getDeviceId() {
        return deviceId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VOLUME_DETACH;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        final StringBuilder sb = new StringBuilder();
        if (id != null) {
            sb.append(": " + id);
        } else if ((deviceId != null) && (virtualMachineId != null)) {
            sb.append(" with device id: " + deviceId + " from vm: " + virtualMachineId);
        } else {
            sb.append(" <error:  either volume id or deviceId/vmId need to be specified>");
        }
        return "detaching volume" + sb.toString();
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Volume;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Volume Id: " + getId() + " VmId: " + getVirtualMachineId());
        final Volume result = _volumeService.detachVolumeFromVM(this);
        if (result != null) {
            final VolumeResponse response = _responseGenerator.createVolumeResponse(ResponseView.Restricted, result);
            response.setResponseName("volume");
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to detach volume");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Long volumeId = getId();
        if (volumeId != null) {
            final Volume volume = _responseGenerator.findVolumeById(volumeId);
            if (volume != null) {
                return volume.getAccountId();
            }
        } else if (getVirtualMachineId() != null) {
            final UserVm vm = _responseGenerator.findUserVmById(getVirtualMachineId());
            if (vm != null) {
                return vm.getAccountId();
            }
        }

        // invalid id, parent this command to SYSTEM so ERROR events are tracked
        return Account.ACCOUNT_ID_SYSTEM;
    }

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }
}
