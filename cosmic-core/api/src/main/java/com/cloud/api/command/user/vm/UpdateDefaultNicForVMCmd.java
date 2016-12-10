package com.cloud.api.command.user.vm;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiConstants.VMDetails;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NicResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.vm.VirtualMachine;

import java.util.ArrayList;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateDefaultNicForVirtualMachine", description = "Changes the default NIC on a VM", responseObject = UserVmResponse.class, responseView = ResponseView
        .Restricted, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class UpdateDefaultNicForVMCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateDefaultNicForVMCmd.class);
    private static final String s_name = "updatedefaultnicforvirtualmachineresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.UUID, entityType = UserVmResponse.class,
            required = true, description = "Virtual Machine ID")
    private Long vmId;

    @Parameter(name = ApiConstants.NIC_ID, type = CommandType.UUID, entityType = NicResponse.class, required = true, description = "NIC ID")
    private Long nicId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "virtualmachine";
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NIC_UPDATE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "Updating NIC " + getNicId() + " on user vm: " + getVmId();
    }

    public Long getNicId() {
        return nicId;
    }

    public Long getVmId() {
        return vmId;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Vm Id: " + getVmId() + " Nic Id: " + getNicId());
        final UserVm result = _userVmService.updateDefaultNicForVirtualMachine(this);
        final ArrayList<VMDetails> dc = new ArrayList<>();
        dc.add(VMDetails.valueOf("nics"));
        final EnumSet<VMDetails> details = EnumSet.copyOf(dc);
        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Restricted, "virtualmachine", details, result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to set default nic for VM. Refer to server logs for details.");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final UserVm vm = _responseGenerator.findUserVmById(getVmId());
        if (vm == null) {
            return Account.ACCOUNT_ID_SYSTEM; // bad id given, parent this command to SYSTEM so ERROR events are tracked
        }
        return vm.getAccountId();
    }
}
