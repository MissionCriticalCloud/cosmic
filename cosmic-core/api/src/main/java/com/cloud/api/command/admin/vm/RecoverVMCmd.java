package com.cloud.api.command.admin.vm;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.UserVmResponse;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "recoverVirtualMachine", group = APICommandGroup.VirtualMachineService, description = "Recovers a virtual machine.", responseObject = UserVmResponse.class, entityType =
        {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class RecoverVMCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(RecoverVMCmd.class.getName());

    private static final String s_name = "recovervirtualmachineresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = UserVmResponse.class, required = true, description = "The ID of the virtual machine")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceAllocationException {
        final UserVm result = _userVmService.recoverVirtualMachine(this);
        if (result != null) {
            final UserVmResponse recoverVmResponse = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", result).get(0);
            recoverVmResponse.setResponseName(getCommandName());
            setResponseObject(recoverVmResponse);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to recover vm");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final UserVm userVm = _entityMgr.findById(UserVm.class, getId());
        if (userVm != null) {
            return userVm.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    public Long getId() {
        return id;
    }
}
