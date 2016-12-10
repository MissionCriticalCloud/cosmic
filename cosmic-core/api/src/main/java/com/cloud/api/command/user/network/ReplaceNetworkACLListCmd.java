package com.cloud.api.command.user.network;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NetworkACLResponse;
import com.cloud.api.response.NetworkResponse;
import com.cloud.api.response.PrivateGatewayResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "replaceNetworkACLList", description = "Replaces ACL associated with a network or private gateway", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ReplaceNetworkACLListCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ReplaceNetworkACLListCmd.class.getName());
    private static final String s_name = "replacenetworkacllistresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ACL_ID, type = CommandType.UUID, entityType = NetworkACLResponse.class, required = true, description = "the ID of the network ACL")
    private long aclId;

    @Parameter(name = ApiConstants.NETWORK_ID, type = CommandType.UUID, entityType = NetworkResponse.class, description = "the ID of the network")
    private Long networkId;

    @Parameter(name = ApiConstants.GATEWAY_ID, type = CommandType.UUID, entityType = PrivateGatewayResponse.class, description = "the ID of the private gateway")
    private Long privateGatewayId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public long getAclId() {
        return aclId;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NETWORK_ACL_REPLACE;
    }

    @Override
    public String getEventDescription() {
        return ("Associating network ACL ID=" + aclId + " with network ID=" + networkId);
    }

    @Override
    public void execute() throws ResourceUnavailableException {
        if (getNetworkId() == null && getPrivateGatewayId() == null) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Network ID and private gateway can't be null at the same time");
        }

        if (getNetworkId() != null && getPrivateGatewayId() != null) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Network ID and private gateway can't be passed at the same time");
        }

        CallContext.current().setEventDetails("Network ACL ID: " + aclId);
        boolean result = false;
        if (getPrivateGatewayId() != null) {
            result = _networkACLService.replaceNetworkACLonPrivateGw(aclId, privateGatewayId);
        } else {
            result = _networkACLService.replaceNetworkACL(aclId, networkId);
        }

        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to replace network ACL");
        }
    }

    public Long getPrivateGatewayId() {
        return privateGatewayId;
    }

    public Long getNetworkId() {
        return networkId;
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
        final Account caller = CallContext.current().getCallingAccount();
        return caller.getAccountId();
    }
}
