package com.cloud.api.command.user.vpn;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.IPAddressResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.RemoteAccessVpn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteRemoteAccessVpn", description = "Destroys a l2tp/ipsec remote access vpn", responseObject = SuccessResponse.class, entityType = {RemoteAccessVpn.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteRemoteAccessVpnCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteRemoteAccessVpnCmd.class.getName());

    private static final String s_name = "deleteremoteaccessvpnresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.PUBLIC_IP_ID,
            type = CommandType.UUID,
            entityType = IPAddressResponse.class,
            required = true,
            description = "public ip address id of the vpn server")
    private Long publicIpId;

    // unexposed parameter needed for events logging
    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.UUID, entityType = AccountResponse.class, expose = false)
    private Long ownerId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_REMOTE_ACCESS_VPN_DESTROY;
    }

    @Override
    public String getEventDescription() {
        return "Delete Remote Access VPN for account " + getEntityOwnerId() + " for  ip id=" + publicIpId;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return _ravService.getRemoteAccessVpn(publicIpId).getNetworkId();
    }

    @Override
    public void execute() throws ResourceUnavailableException {
        if (!_ravService.destroyRemoteAccessVpnForIp(publicIpId, CallContext.current().getCallingAccount())) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete remote access vpn");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        if (ownerId == null) {
            final RemoteAccessVpn vpnEntity = _ravService.getRemoteAccessVpn(publicIpId);
            if (vpnEntity != null) {
                return vpnEntity.getAccountId();
            }

            throw new InvalidParameterValueException("The specified public ip is not allocated to any account");
        }
        return ownerId;
    }
}
