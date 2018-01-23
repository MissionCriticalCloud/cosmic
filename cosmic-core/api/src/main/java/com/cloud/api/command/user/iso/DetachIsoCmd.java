package com.cloud.api.command.user.iso;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vm.DeployVMCmd;
import com.cloud.api.response.UserVmResponse;
import com.cloud.event.EventTypes;
import com.cloud.uservm.UserVm;
import com.cloud.utils.exception.InvalidParameterValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "detachIso", group = APICommandGroup.ISOService, description = "Detaches any ISO file (if any) currently attached to a virtual machine.", responseObject = UserVmResponse.class, responseView =
        ResponseView.Restricted,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class DetachIsoCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DetachIsoCmd.class.getName());

    private static final String s_name = "detachisoresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.UUID, entityType = UserVmResponse.class,
            required = true, description = "The ID of the virtual machine")
    protected Long virtualMachineId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ISO_DETACH;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "detaching ISO from VM: " + getVirtualMachineId();
    }

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }

    @Override
    public void execute() {
        final boolean result = _templateService.detachIso(virtualMachineId);
        if (result) {
            final UserVm userVm = _entityMgr.findById(UserVm.class, virtualMachineId);
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Restricted, "virtualmachine", userVm).get(0);
            response.setResponseName(DeployVMCmd.getResultObjectName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to detach ISO");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final UserVm vm = _entityMgr.findById(UserVm.class, getVirtualMachineId());
        if (vm != null) {
            return vm.getAccountId();
        } else {
            throw new InvalidParameterValueException("Unable to find VM by ID " + getVirtualMachineId());
        }
    }
}
