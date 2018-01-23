package com.cloud.api.command.admin.vm;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants.VMDetails;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vm.AddNicToVMCmd;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import java.util.ArrayList;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addNicToVirtualMachine", group = APICommandGroup.VirtualMachineService, description = "Adds VM to specified network by creating a NIC", responseObject = UserVmResponse.class, responseView = ResponseView
        .Full, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class AddNicToVMCmdByAdmin extends AddNicToVMCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddNicToVMCmdByAdmin.class);

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Vm Id: " + getVmId() + " Network Id: " + getNetworkId());
        final UserVm result = _userVmService.addNicToVirtualMachine(this);
        final ArrayList<VMDetails> dc = new ArrayList<>();
        dc.add(VMDetails.valueOf("nics"));
        final EnumSet<VMDetails> details = EnumSet.copyOf(dc);
        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", details, result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add NIC to vm. Refer to server logs for details.");
        }
    }
}
