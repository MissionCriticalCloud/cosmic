package com.cloud.api.command.user.vm;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "resetPasswordForVirtualMachine", group = APICommandGroup.VirtualMachineService, responseObject = UserVmResponse.class, description = "Resets the password for virtual machine. " +
        "The virtual machine must be in a \"Stopped\" state and the template must already " +
        "support this feature for this command to take effect. [async]", responseView = ResponseView.Restricted, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class ResetVMPasswordCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ResetVMPasswordCmd.class.getName());

    private static final String s_name = "resetpasswordforvirtualmachineresponse";
    // unexposed parameter needed for serializing/deserializing the command
    @Parameter(name = ApiConstants.PASSWORD, type = CommandType.STRING, expose = false)
    protected String password;
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = UserVmResponse.class,
            required = true, description = "The ID of the virtual machine")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_RESETPASSWORD;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "resetting password for vm: " + getId();
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.VirtualMachine;
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException {
        password = _mgr.generateRandomPassword();
        CallContext.current().setEventDetails("Vm Id: " + getId());
        final UserVm result = _userVmService.resetVMPassword(this, password);
        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Restricted, "virtualmachine", result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to reset vm password");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final UserVm vm = _responseGenerator.findUserVmById(getId());
        if (vm != null) {
            return vm.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
