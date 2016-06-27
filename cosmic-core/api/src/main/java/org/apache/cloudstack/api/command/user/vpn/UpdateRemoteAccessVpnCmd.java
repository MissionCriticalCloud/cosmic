package org.apache.cloudstack.api.command.user.vpn;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.network.RemoteAccessVpn;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCustomIdCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.RemoteAccessVpnResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateRemoteAccessVpn", description = "Updates remote access vpn", responseObject = RemoteAccessVpnResponse.class, since = "4.4",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateRemoteAccessVpnCmd extends BaseAsyncCustomIdCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateRemoteAccessVpnCmd.class.getName());

    private static final String s_name = "updateremoteaccessvpnresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, required = true, entityType = RemoteAccessVpnResponse.class, description = "id of the remote access vpn")
    private Long id;

    // unexposed parameter needed for events logging
    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.UUID, entityType = AccountResponse.class, expose = false)
    private Long ownerId;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the vpn to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_REMOTE_ACCESS_VPN_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Updating remote access vpn id=" + id;
    }

    @Override
    public void execute() {
        final RemoteAccessVpn result = _ravService.updateRemoteAccessVpn(id, this.getCustomId(), getDisplay());
        final RemoteAccessVpnResponse response = _responseGenerator.createRemoteAccessVpnResponse(result);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        if (ownerId == null) {
            final RemoteAccessVpn vpnEntity = _ravService.getRemoteAccessVpnById(id);
            if (vpnEntity != null) {
                return vpnEntity.getAccountId();
            }

            throw new InvalidParameterValueException("The specified id is invalid");
        }
        return ownerId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Boolean getDisplay() {
        return display;
    }

    @Override
    public void checkUuid() {
        if (this.getCustomId() != null) {
            _uuidMgr.checkUuid(this.getCustomId(), RemoteAccessVpn.class);
        }
    }
}
