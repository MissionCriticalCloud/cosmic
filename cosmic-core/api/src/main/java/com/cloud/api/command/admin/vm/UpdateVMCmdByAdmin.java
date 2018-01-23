package com.cloud.api.command.admin.vm;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vm.UpdateVMCmd;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateVirtualMachine", group = APICommandGroup.VirtualMachineService, description = "Updates properties of a virtual machine. The VM has to be stopped and restarted for the " +
        "new properties to take effect. UpdateVirtualMachine does not first check whether the VM is stopped. " +
        "Therefore, stop the VM manually before issuing this call.", responseObject = UserVmResponse.class, responseView = ResponseView.Full, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class UpdateVMCmdByAdmin extends UpdateVMCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateVMCmdByAdmin.class.getName());

    @Override
    public void execute() throws ResourceUnavailableException,
            InsufficientCapacityException, ServerApiException {
        CallContext.current().setEventDetails("Vm Id: " + getId());
        final UserVm result = _userVmService.updateVirtualMachine(this);
        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update vm");
        }
    }
}
