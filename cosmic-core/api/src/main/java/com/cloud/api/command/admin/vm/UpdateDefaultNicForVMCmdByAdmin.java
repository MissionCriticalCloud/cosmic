package com.cloud.api.command.admin.vm;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants.VMDetails;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vm.UpdateDefaultNicForVMCmd;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.uservm.UserVm;

import java.util.ArrayList;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateDefaultNicForVirtualMachine", group = APICommandGroup.VirtualMachineService, description = "Changes the default NIC on a VM", responseObject = UserVmResponse.class,
        responseView = ResponseView.Full,
        entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class UpdateDefaultNicForVMCmdByAdmin extends UpdateDefaultNicForVMCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateDefaultNicForVMCmdByAdmin.class);

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Vm Id: " + getVmId() + " Nic Id: " + getNicId());
        final UserVm result = _userVmService.updateDefaultNicForVirtualMachine(this);
        final ArrayList<VMDetails> dc = new ArrayList<>();
        dc.add(VMDetails.valueOf("nics"));
        final EnumSet<VMDetails> details = EnumSet.copyOf(dc);
        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", details, result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to set default nic for VM. Refer to server logs for details.");
        }
    }
}
