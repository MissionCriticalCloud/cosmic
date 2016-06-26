package org.apache.cloudstack.api.command.admin.iso;

import com.cloud.uservm.UserVm;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.iso.AttachIsoCmd;
import org.apache.cloudstack.api.command.user.vm.DeployVMCmd;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "attachIso", description = "Attaches an ISO to a virtual machine.", responseObject = UserVmResponse.class, responseView = ResponseView.Full,
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
