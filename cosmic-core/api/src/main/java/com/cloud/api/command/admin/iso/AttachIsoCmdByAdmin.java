package com.cloud.api.command.admin.iso;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.iso.AttachIsoCmd;
import com.cloud.api.command.user.vm.DeployVMCmd;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.uservm.UserVm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "attachIso", group = APICommandGroup.ISOService, description = "Attaches an ISO to a virtual machine.", responseObject = UserVmResponse.class, responseView = ResponseView.Full,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class AttachIsoCmdByAdmin extends AttachIsoCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AttachIsoCmdByAdmin.class.getName());

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Vm Id: " + getVirtualMachineId() + " ISO Id: " + getId());
        final boolean result = _templateService.attachIso(id, virtualMachineId);
        if (result) {
            final UserVm userVm = _responseGenerator.findUserVmById(virtualMachineId);
            if (userVm != null) {
                final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", userVm).get(0);
                response.setResponseName(DeployVMCmd.getResultObjectName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to attach iso");
            }
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to attach iso");
        }
    }
}
