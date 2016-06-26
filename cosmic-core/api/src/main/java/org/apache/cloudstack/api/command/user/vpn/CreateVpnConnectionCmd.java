package org.apache.cloudstack.api.command.user.vpn;

import com.cloud.event.EventTypes;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.network.Site2SiteVpnGateway;
import com.cloud.network.vpc.Vpc;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.Site2SiteCustomerGatewayResponse;
import org.apache.cloudstack.api.response.Site2SiteVpnConnectionResponse;
import org.apache.cloudstack.api.response.Site2SiteVpnGatewayResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createVpnConnection", description = "Create site to site vpn connection", responseObject = Site2SiteVpnConnectionResponse.class, entityType =
        {Site2SiteVpnConnection.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateVpnConnectionCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateVpnConnectionCmd.class.getName());

    private static final String s_name = "createvpnconnectionresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.S2S_VPN_GATEWAY_ID,
            type = CommandType.UUID,
            entityType = Site2SiteVpnGatewayResponse.class,
            required = true,
            description = "id of the vpn gateway")
    private Long vpnGatewayId;

    @Parameter(name = ApiConstants.S2S_CUSTOMER_GATEWAY_ID,
            type = CommandType.UUID,
            entityType = Site2SiteCustomerGatewayResponse.class,
            required = true,
            description = "id of the customer gateway")
    private Long customerGatewayId;

    @Parameter(name = ApiConstants.PASSIVE, type = CommandType.BOOLEAN, required = false, description = "connection is passive or not")
    private Boolean passive;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "an optional field, whether to the display the vpn to the end user or not", since = "4" +
            ".4", authorized = {RoleType.Admin})
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getVpnGatewayId() {
        return vpnGatewayId;
    }

    public Long getCustomerGatewayId() {
        return customerGatewayId;
    }

    public boolean isPassive() {
        if (passive == null) {
            return false;
        }
        return passive;
    }

    @Deprecated
    public Boolean getDisplay() {
        return display;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_S2S_VPN_CONNECTION_CREATE;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventDescription() {
        return "Create site-to-site VPN connection for account " + getEntityOwnerId();
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.vpcSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        final Site2SiteVpnGateway vpnGw = getVpnGateway();
        if (vpnGw != null) {
            return vpnGw.getVpcId();
        }
        return null;
    }

    private Site2SiteVpnGateway getVpnGateway() {
        return _s2sVpnService.getVpnGateway(vpnGatewayId);
    }

    @Override
    public void create() {
        try {
            final Site2SiteVpnConnection conn = _s2sVpnService.createVpnConnection(this);
            if (conn != null) {
                setEntityId(conn.getId());
                setEntityUuid(conn.getUuid());
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create site to site vpn connection");
            }
        } catch (final NetworkRuleConflictException e) {
            s_logger.info("Network rule conflict: " + e.getMessage());
            s_logger.trace("Network Rule Conflict: ", e);
            throw new ServerApiException(ApiErrorCode.NETWORK_RULE_CONFLICT_ERROR, e.getMessage());
        }
    }

    @Override
    public void execute() {
        try {
            final Site2SiteVpnConnection result = _s2sVpnService.startVpnConnection(getEntityId());
            if (result != null) {
                final Site2SiteVpnConnectionResponse response = _responseGenerator.createSite2SiteVpnConnectionResponse(result);
                response.setResponseName(getCommandName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create site to site vpn connection");
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
        final Site2SiteVpnGateway vpnGw = getVpnGateway();
        if (vpnGw != null) {
            final Vpc vpc = _entityMgr.findById(Vpc.class, getVpnGateway().getVpcId());
            return vpc.getAccountId();
        }
        return -1;
    }

    @Override
    public boolean isDisplay() {
        if (display != null) {
            return display;
        } else {
            return true;
        }
    }
}
