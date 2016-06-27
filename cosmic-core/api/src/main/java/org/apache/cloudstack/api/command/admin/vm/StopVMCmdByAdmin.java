package org.apache.cloudstack.api.command.admin.vm;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.vm.StopVMCmd;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "stopVirtualMachine", responseObject = UserVmResponse.class, description = "Stops a virtual machine.", responseView = ResponseView.Full, entityType =
        {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class StopVMCmdByAdmin extends StopVMCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(StopVMCmdByAdmin.class.getName());

    @Override
    public void execute() throws ServerApiException, ConcurrentOperationException {
        CallContext.current().setEventDetails("Vm Id: " + getId());
        final UserVm result;

        result = _userVmService.stopVirtualMachine(getId(), isForced());

        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to stop vm");
        }
    }
}
