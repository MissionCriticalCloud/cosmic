package org.apache.cloudstack.api.command.admin.vm;

import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.api.response.VMUserDataResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "getVirtualMachineUserData", description = "Returns user data associated with the VM", responseObject = VMUserDataResponse.class, since = "4.4",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class GetVMUserDataCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(GetVMUserDataCmd.class);
    private static final String s_name = "getvirtualmachineuserdataresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.UUID, entityType = UserVmResponse.class, required = true, description = "The ID of the virtual machine")
    private Long vmId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final String userData = _userVmService.getVmUserData(getId());
        final VMUserDataResponse resp = new VMUserDataResponse();
        resp.setVmId(_entityMgr.findById(UserVm.class, getId()).getUuid());
        resp.setUserData(userData);
        resp.setObjectName("virtualmachineuserdata");
        resp.setResponseName(getCommandName());
        this.setResponseObject(resp);
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public long getId() {
        return vmId;
    }

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
}
