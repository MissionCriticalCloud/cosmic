package com.cloud.api.command.user.nat;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.IPAddressResponse;
import com.cloud.api.response.NetworkResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.utils.exception.InvalidParameterValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "enableStaticNat", description = "Enables static NAT for given IP address", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class EnableStaticNatCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(EnableStaticNatCmd.class);

    private static final String s_name = "enablestaticnatresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.IP_ADDRESS_ID, type = CommandType.UUID, entityType = IPAddressResponse.class, required = true, description = "the public IP "
            + "address ID for which static NAT feature is being enabled")
    private Long ipAddressId;

    @Parameter(name = ApiConstants.VIRTUAL_MACHINE_ID, type = CommandType.UUID, entityType = UserVmResponse.class, required = true, description = "the ID of "
            + "the virtual machine for enabling static NAT feature")
    private Long virtualMachineId;

    @Parameter(name = ApiConstants.NETWORK_ID,
            type = CommandType.UUID,
            entityType = NetworkResponse.class,
            description = "The network of the VM the static NAT will be enabled for."
                    + " Required when public IP address is not associated with any guest network yet (VPC case)")
    private Long networkId;
    @Parameter(name = ApiConstants.VM_GUEST_IP,
            type = CommandType.STRING,
            required = false,
            description = "VM guest NIC secondary IP address for the port forwarding rule")
    private String vmSecondaryIp;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceUnavailableException {
        try {
            final boolean result = _rulesService.enableStaticNat(ipAddressId, virtualMachineId, getNetworkId(), getVmSecondaryIp());
            if (result) {
                final SuccessResponse response = new SuccessResponse(getCommandName());
                this.setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to enable static NAT");
            }
        } catch (final NetworkRuleConflictException ex) {
            s_logger.info("Network rule conflict: " + ex.getMessage());
            s_logger.trace("Network Rule Conflict: ", ex);
            throw new ServerApiException(ApiErrorCode.NETWORK_RULE_CONFLICT_ERROR, ex.getMessage());
        }
    }

    public long getNetworkId() {
        final IpAddress ip = _entityMgr.findById(IpAddress.class, getIpAddressId());
        Long ntwkId = null;

        if (ip.getAssociatedWithNetworkId() != null) {
            ntwkId = ip.getAssociatedWithNetworkId();
        } else {
            ntwkId = networkId;
        }

        // in case of portable public IP, network ID passed takes precedence
        if (ip.isPortable() && networkId != null) {
            ntwkId = networkId;
        }

        if (ntwkId == null) {
            throw new InvalidParameterValueException("Unable to enable static NAT for the ipAddress id=" + ipAddressId +
                    " as IP is not associated with any network and no networkId is passed in");
        }
        return ntwkId;
    }

    public String getVmSecondaryIp() {
        if (vmSecondaryIp == null) {
            return null;
        }
        return vmSecondaryIp;
    }

    public Long getIpAddressId() {
        return ipAddressId;
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
        final UserVm userVm = _entityMgr.findById(UserVm.class, getVirtualMachineId());
        if (userVm != null) {
            return userVm.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    public Long getVirtualMachineId() {
        return virtualMachineId;
    }
}
