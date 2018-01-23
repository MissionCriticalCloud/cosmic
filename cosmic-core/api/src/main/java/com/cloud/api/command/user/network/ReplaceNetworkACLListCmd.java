package com.cloud.api.command.user.network;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.IPAddressResponse;
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

@APICommand(name = "replaceNetworkACLList", group = APICommandGroup.NetworkACLService, description = "Replaces ACL associated with a network or private gateway", responseObject = SuccessResponse.class,
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

    @Parameter(name = ApiConstants.PUBLIC_IP_ID, type = CommandType.UUID, entityType = IPAddressResponse.class, description = "the ID of the public ip")
    private Long publicIpId;

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
        validateAPICall();

        CallContext.current().setEventDetails("Network ACL ID: " + aclId);
        boolean result = false;
        if (getPrivateGatewayId() != null) {
            result = _networkACLService.replaceNetworkACLonPrivateGw(aclId, privateGatewayId);
        } else if (getNetworkId() != null) {
            result = _networkACLService.replaceNetworkACL(aclId, networkId);
        } else if (getPublicIpId() != null) {
            result = _networkACLService.replacePublicIpACL(aclId, publicIpId);
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

    public Long getPublicIpId() {
        return publicIpId;
    }

    void validateAPICall() {
        int noIds = 0;
        if (getNetworkId() != null) {
            noIds++;
        }
        if (getPrivateGatewayId() != null) {
            noIds++;
        }
        if (getPublicIpId() != null) {
            noIds++;
        }

        if (noIds != 1) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Make sure that one and only one Private Gateway, Network or Public IP ID is passed.");
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
        final Account caller = CallContext.current().getCallingAccount();
        return caller.getAccountId();
    }
}
