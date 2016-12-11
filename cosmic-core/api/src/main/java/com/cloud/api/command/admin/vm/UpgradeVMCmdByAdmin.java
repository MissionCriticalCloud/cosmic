package com.cloud.api.command.admin.vm;

import com.cloud.api.APICommand;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vm.UpgradeVMCmd;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.offering.ServiceOffering;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "changeServiceForVirtualMachine", responseObject = UserVmResponse.class, description = "Changes the service offering for a virtual machine. " +
        "The virtual machine must be in a \"Stopped\" state for " +
        "this command to take effect.", responseView = ResponseView.Full, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class UpgradeVMCmdByAdmin extends UpgradeVMCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpgradeVMCmdByAdmin.class.getName());

    @Override
    public void execute() throws ResourceAllocationException {
        CallContext.current().setEventDetails("Vm Id: " + getId());

        final ServiceOffering serviceOffering = _entityMgr.findById(ServiceOffering.class, serviceOfferingId);
        if (serviceOffering == null) {
            throw new InvalidParameterValueException("Unable to find service offering: " + serviceOfferingId);
        }

        final UserVm result = _userVmService.upgradeVirtualMachine(this);
        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to upgrade vm");
        }
    }
}
