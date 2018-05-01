package com.cloud.api.command.admin.vm;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vm.RestoreVMCmd;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "restoreVirtualMachine", group = APICommandGroup.VirtualMachineService, description = "Restore a VM to original template/ISO or new template/ISO", responseObject = UserVmResponse
        .class, since = "3.0.0",
        responseView = ResponseView.Full, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = true)
public class RestoreVMCmdByAdmin extends RestoreVMCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(RestoreVMCmdByAdmin.class);

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        final UserVm result;
        CallContext.current().setEventDetails("Vm Id: " + getVmId());
        result = _userVmService.restoreVM(this);
        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to restore vm " + getVmId());
        }
    }
}
