package org.apache.cloudstack.api.command.user.vpn;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.RemoteAccessVpn;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.context.CallContext;

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
