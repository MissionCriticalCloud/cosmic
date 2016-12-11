package com.cloud.api.command.admin.vm;

import com.cloud.api.APICommand;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vm.RebootVMCmd;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "rebootVirtualMachine", description = "Reboots a virtual machine.", responseObject = UserVmResponse.class, responseView = ResponseView.Full, entityType =
        {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class RebootVMCmdByAdmin extends RebootVMCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(RebootVMCmdByAdmin.class.getName());

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException {
        CallContext.current().setEventDetails("Vm Id: " + getId());
        final UserVm result;
        result = _userVmService.rebootVirtualMachine(this);

        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to reboot vm instance");
        }
    }
}
