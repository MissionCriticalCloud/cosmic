package org.apache.cloudstack.api.command.user.nat;

import com.cloud.event.EventTypes;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.firewall.DeletePortForwardingRuleCmd;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.SuccessResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "disableStaticNat", description = "Disables static rule for given IP address", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DisableStaticNatCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeletePortForwardingRuleCmd.class.getName());
    private static final String s_name = "disablestaticnatresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.IP_ADDRESS_ID,
            type = CommandType.UUID,
            entityType = IPAddressResponse.class,
            required = true,
            description = "the public IP address ID for which static NAT feature is being disabled")
    private Long ipAddressId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getIpAddress() {
        return ipAddressId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_DISABLE_STATIC_NAT;
    }

    @Override
    public String getEventDescription() {
        return ("Disabling static NAT for IP ID=" + ipAddressId);
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return getIp().getAssociatedWithNetworkId();
    }

    private IpAddress getIp() {
        final IpAddress ip = _networkService.getIp(ipAddressId);
        if (ip == null) {
            throw new InvalidParameterValueException("Unable to find IP address by ID " + ipAddressId);
        }
        return ip;
    }

    @Override
    public void execute() throws ResourceUnavailableException, NetworkRuleConflictException, InsufficientAddressCapacityException {
        final boolean result = _rulesService.disableStaticNat(ipAddressId);

        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to disable static NAT");
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
        return _entityMgr.findById(IpAddress.class, ipAddressId).getAccountId();
    }
}
