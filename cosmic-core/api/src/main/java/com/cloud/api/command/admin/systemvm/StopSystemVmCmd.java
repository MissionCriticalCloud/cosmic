package com.cloud.api.command.admin.systemvm;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.SystemVmResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import com.cloud.vm.VirtualMachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "stopSystemVm", group = APICommandGroup.SystemVMService, description = "Stops a system VM.", responseObject = SystemVmResponse.class, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class StopSystemVmCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(StopSystemVmCmd.class.getName());

    private static final String s_name = "stopsystemvmresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = SystemVmResponse.class,
            required = true,
            description = "The ID of the system virtual machine")
    private Long id;

    @Parameter(name = ApiConstants.FORCED, type = CommandType.BOOLEAN, required = false, description = "Force stop the VM.  The caller knows the VM is stopped.")
    private Boolean forced;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        final VirtualMachine.Type type = _mgr.findSystemVMTypeById(getId());
        if (type == VirtualMachine.Type.ConsoleProxy) {
            return EventTypes.EVENT_PROXY_STOP;
        } else {
            return EventTypes.EVENT_SSVM_STOP;
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getEventDescription() {
        return "stopping system vm: " + getId();
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.SystemVm;
    }

    public boolean isForced() {
        return (forced != null) ? forced : false;
    }

    @Override
    public void execute() throws ResourceUnavailableException, ConcurrentOperationException {
        CallContext.current().setEventDetails("Vm Id: " + getId());
        final VirtualMachine result = _mgr.stopSystemVM(this);
        if (result != null) {
            final SystemVmResponse response = _responseGenerator.createSystemVmResponse(result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Fail to stop system vm");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Account account = CallContext.current().getCallingAccount();
        if (account != null) {
            return account.getId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }
}
