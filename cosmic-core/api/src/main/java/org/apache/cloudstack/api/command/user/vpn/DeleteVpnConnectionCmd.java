package org.apache.cloudstack.api.command.user.vpn;

import com.cloud.event.EventTypes;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.Site2SiteVpnConnectionResponse;
import org.apache.cloudstack.api.response.SuccessResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteVpnConnection", description = "Delete site to site vpn connection", responseObject = SuccessResponse.class, entityType = {Site2SiteVpnConnection.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteVpnConnectionCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteVpnConnectionCmd.class.getName());

    private static final String s_name = "deletevpnconnectionresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = Site2SiteVpnConnectionResponse.class, required = true, description = "id of vpn connection")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_S2S_VPN_CONNECTION_DELETE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "Delete site-to-site VPN connection for account " + getEntityOwnerId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() {
        try {
            final boolean result = _s2sVpnService.deleteVpnConnection(this);
            if (result) {
                final SuccessResponse response = new SuccessResponse(getCommandName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete site to site VPN connection");
            }
        } catch (final ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Site2SiteVpnConnection conn = _entityMgr.findById(Site2SiteVpnConnection.class, getId());
        if (conn != null) {
            return conn.getAccountId();
        }
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
