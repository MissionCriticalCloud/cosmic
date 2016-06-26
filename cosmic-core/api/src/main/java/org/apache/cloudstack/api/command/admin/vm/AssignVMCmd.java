package org.apache.cloudstack.api.command.admin.vm;

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
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.SecurityGroupResponse;
import org.apache.cloudstack.api.response.UserVmResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "assignVirtualMachine",
        description = "Change ownership of a VM from one account to another. This API is available for Basic zones with security groups and Advanced zones with guest networks. A" +
                " root administrator can reassign a VM from any account to any other account in any domain. A domain administrator can reassign a VM to any account in the same " +
                "domain.",
        responseObject = UserVmResponse.class,
        since = "3.0.0", entityType = {VirtualMachine.class},
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = true)
public class AssignVMCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AssignVMCmd.class.getName());

    private static final String s_name = "assignvirtualmachineresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID,
            type = CommandType.UUID,
            entityType = UserVmResponse.class,
            required = true,
            description = "id of the VM to be moved")
    private Long virtualMachineId;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, required = true, description = "account name of the new VM owner.")
    private String accountName;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, required = true, description = "domain id of the new VM owner.")
    private Long domainId;

    //Network information
    @Parameter(name = ApiConstants.NETWORK_IDS,
            type = CommandType.LIST,
            collectionType = CommandType.UUID,
            entityType = NetworkResponse.class,
            description = "list of new network ids in which the moved VM will participate. In case no network ids are provided the VM will be part of the default network for " +
                    "that zone. "
                    +
                    "In case there is no network yet created for the new account the default network will be created.")
    private List<Long> networkIds;

    @Parameter(name = ApiConstants.SECURITY_GROUP_IDS,
            type = CommandType.LIST,
            collectionType = CommandType.UUID,
            entityType = SecurityGroupResponse.class,
            description = "list of security group ids to be applied on the virtual machine. " +
                    "In case no security groups are provided the VM is part of the default security group.")
    private List<Long> securityGroupIdList;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public List<Long> getNetworkIds() {
        return networkIds;
    }

    public List<Long> getSecurityGroupIdList() {
        return securityGroupIdList;
    }

    @Override
    public void execute() {
        try {
            final UserVm userVm = _userVmService.moveVMToUser(this);
            if (userVm == null) {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to move vm");
            }
            final UserVmResponse response = _responseGenerator.createUserVmResponse(ResponseView.Full, "virtualmachine", userVm).get(0);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to move vm " + e.getMessage());
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
        final UserVm vm = _responseGenerator.findUserVmById(getVmId());
        if (vm != null) {
            return vm.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    public Long getVmId() {
        return virtualMachineId;
    }
}
