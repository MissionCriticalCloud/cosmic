package org.apache.cloudstack.api.command.admin.vm;

import com.cloud.exception.ResourceAllocationException;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.UserVmResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "recoverVirtualMachine", description = "Recovers a virtual machine.", responseObject = UserVmResponse.class, entityType = {VirtualMachine.class},
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
