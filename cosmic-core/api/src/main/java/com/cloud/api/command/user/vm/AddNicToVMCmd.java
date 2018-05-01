package com.cloud.api.command.user.vm;

import com.cloud.acl.SecurityChecker.AccessType;
import com.cloud.api.ACL;
import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiConstants.VMDetails;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NetworkResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.VirtualMachine;

import java.util.ArrayList;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addNicToVirtualMachine", group = APICommandGroup.VirtualMachineService, description = "Adds VM to specified network by creating a NIC", responseObject = UserVmResponse.class,
        responseView = ResponseView
                .Restricted, entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class AddNicToVMCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddNicToVMCmd.class);
    private static final String s_name = "addnictovirtualmachineresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @ACL(accessType = AccessType.OperateEntry)
    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.UUID, entityType = UserVmResponse.class,
            required = true, description = "Virtual Machine ID")
    private Long vmId;

    @Parameter(name = ApiConstants.NETWORK_ID, type = CommandType.UUID, entityType = NetworkResponse.class, required = true, description = "Network ID")
    private Long netId;

    @Parameter(name = ApiConstants.IP_ADDRESS, type = CommandType.STRING, description = "IP Address for the new network")
    private String ipaddr;

    @Parameter(name = ApiConstants.MAC_ADDRESS, type = CommandType.STRING, description = "MAC-Address for the new network")
    private String macaddr;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "virtualmachine";
    }

    public String getIpAddress() {
        return ipaddr;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NIC_CREATE;
    }

    public String getMacAddress() {
        if (macaddr == null) {
            return null;
        }
        if (!NetUtils.isValidMac(macaddr)) {
            throw new InvalidParameterValueException("MAC-Address is not valid: " + macaddr);
        } else if (!NetUtils.isUnicastMac(macaddr)) {
            throw new InvalidParameterValueException("MAC-Address is not unicast: " + macaddr);
        }
        return NetUtils.standardizeMacAddress(macaddr);
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "Adding network " + getNetworkId() + " to user vm: " + getVmId();
    }

    public Long getNetworkId() {
        return netId;
    }

    public Long getVmId() {
        return vmId;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Vm Id: " + getVmId() + " Network Id: " + getNetworkId());
        final UserVm result = _userVmService.addNicToVirtualMachine(this);
        final ArrayList<VMDetails> dc = new ArrayList<>();
        dc.add(VMDetails.valueOf("nics"));
        final EnumSet<VMDetails> details = EnumSet.copyOf(dc);
        if (result != null) {
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Restricted, "virtualmachine", details, result).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add NIC to vm. Refer to server logs for details.");
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
