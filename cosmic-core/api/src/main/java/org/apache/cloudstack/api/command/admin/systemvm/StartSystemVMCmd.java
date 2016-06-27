package org.apache.cloudstack.api.command.admin.systemvm;

import com.cloud.event.EventTypes;
import com.cloud.user.Account;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.ACL;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.SystemVmResponse;
import org.apache.cloudstack.context.CallContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "startSystemVm", responseObject = SystemVmResponse.class, description = "Starts a system virtual machine.", entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class StartSystemVMCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(StartSystemVMCmd.class.getName());

    private static final String s_name = "startsystemvmresponse";

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

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "systemvm";
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        final VirtualMachine.Type type = _mgr.findSystemVMTypeById(getId());
        if (type == VirtualMachine.Type.ConsoleProxy) {
            return EventTypes.EVENT_PROXY_START;
        } else {
            return EventTypes.EVENT_SSVM_START;
        }
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getEventDescription() {
        return "starting system vm: " + getId();
    }

    @Override
    public Long getInstanceId() {
        return getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.SystemVm;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Vm Id: " + getId());
        final VirtualMachine instance = _mgr.startSystemVM(getId());
        if (instance != null) {
            final SystemVmResponse response = _responseGenerator.createSystemVmResponse(instance);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Fail to start system vm");
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
