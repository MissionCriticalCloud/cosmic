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
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.legacymodel.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "destroyVirtualMachine", group = APICommandGroup.VirtualMachineService, description = "Destroys a virtual machine.", responseObject = UserVmResponse.class, responseView =
        ResponseView.Restricted,
        entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = true)
public class DestroyVMCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DestroyVMCmd.class.getName());

    private static final String s_name = "destroyvirtualmachineresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = UserVmResponse.class,
            required = true, description = "The ID of the virtual machine")
    private Long id;

    @Parameter(name = ApiConstants.EXPUNGE,
            type = CommandType.BOOLEAN,
            description = "If true is passed, the vm is expunged immediately. False by default.",
            since = "4.2.1")
    private Boolean expunge;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public boolean getExpunge() {
        if (expunge == null) {
            return false;
        }
        return expunge;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VM_DESTROY;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "destroying vm: " + getId();
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
    public void execute() throws ResourceUnavailableException, ConcurrentOperationException {
        CallContext.current().setEventDetails("Vm Id: " + getId());
        final UserVm result = _userVmService.destroyVm(this);

        UserVmResponse response = new UserVmResponse();
        if (result != null) {
            final List<UserVmResponse> responses = _responseGenerator.createUserVmResponse(ResponseView.Restricted, "virtualmachine", result);
            if (responses != null && !responses.isEmpty()) {
                response = responses.get(0);
            }
            response.setResponseName("virtualmachine");
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to destroy vm");
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
