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
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.model.enumeration.VirtualMachineType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "destroySystemVm", group = APICommandGroup.SystemVMService, responseObject = SystemVmResponse.class, description = "Destroyes a system virtual machine.", entityType =
        {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DestroySystemVmCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DestroySystemVmCmd.class.getName());

    private static final String s_name = "destroysystemvmresponse";

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID,
            type = CommandType.UUID,
            entityType = SystemVmResponse.class,
            required = true,
            description = "The ID of the system virtual machine")
    private Long id;

    public static String getResultObjectName() {
        return "systemvm";
    }

    @Override
    public String getEventType() {
        final VirtualMachineType type = _mgr.findSystemVMTypeById(getId());
        if (type == VirtualMachineType.ConsoleProxy) {
            return EventTypes.EVENT_PROXY_DESTROY;
        } else {
            return EventTypes.EVENT_SSVM_DESTROY;
        }
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getEventDescription() {
        return "destroying system vm: " + getId();
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
        final VirtualMachine instance = _mgr.destroySystemVM(this);
        if (instance != null) {
            final SystemVmResponse response = _responseGenerator.createSystemVmResponse(instance);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Fail to destroy system vm");
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
