package org.apache.cloudstack.api.command.admin.vm;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.vm.DestroyVMCmd;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.context.CallContext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "destroyVirtualMachine", description = "Destroys a virtual machine. Once destroyed, only the administrator can recover it.", responseObject = UserVmResponse
        .class, responseView = ResponseView.Full, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = true)
public class DestroyVMCmdByAdmin extends DestroyVMCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DestroyVMCmdByAdmin.class.getName());

    @Override
    public void execute() throws ResourceUnavailableException, ConcurrentOperationException {
        CallContext.current().setEventDetails("Vm Id: " + getId());
        final UserVm result = _userVmService.destroyVm(this);

        UserVmResponse response = new UserVmResponse();
        if (result != null) {
            final List<UserVmResponse> responses = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", result);
            if (responses != null && !responses.isEmpty()) {
                response = responses.get(0);
            }
            response.setResponseName("virtualmachine");
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to destroy vm");
        }
    }
}
