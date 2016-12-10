package com.cloud.api.command.admin.vm;

import com.cloud.api.APICommand;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vm.ResetVMPasswordCmd;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "resetPasswordForVirtualMachine", responseObject = UserVmResponse.class, description = "Resets the password for virtual machine. " +
        "The virtual machine must be in a \"Stopped\" state and the template must already " +
        "support this feature for this command to take effect. [async]", responseView = ResponseView.Full, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class ResetVMPasswordCmdByAdmin extends ResetVMPasswordCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ResetVMPasswordCmdByAdmin.class.getName());

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException {
        password = _mgr.generateRandomPassword();
        CallContext.current().setEventDetails("Vm Id: " + getId());
        final UserVm result = _userVmService.resetVMPassword(this, password);
        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to reset vm password");
        }
    }
}
