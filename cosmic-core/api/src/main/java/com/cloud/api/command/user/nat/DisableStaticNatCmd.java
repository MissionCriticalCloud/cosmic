package com.cloud.api.command.user.nat;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.firewall.DeletePortForwardingRuleCmd;
import com.cloud.api.response.IPAddressResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.exceptions.InsufficientAddressCapacityException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.NetworkRuleConflictException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.network.IpAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "disableStaticNat", group = APICommandGroup.NATService, description = "Disables static rule for given IP address", responseObject = SuccessResponse.class,
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
