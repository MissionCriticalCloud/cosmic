package com.cloud.api.command.admin.vm;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vm.DestroyVMCmd;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.uservm.UserVm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "destroyVirtualMachine", group = APICommandGroup.VirtualMachineService, description = "Destroys a virtual machine. Once destroyed, only the administrator can recover it.",
        responseObject = UserVmResponse
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
